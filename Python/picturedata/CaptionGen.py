import pandas as pd
import numpy as np
import cv2
import pickle
import os
from keras.preprocessing import image, sequence
from keras.applications.vgg16 import VGG16, preprocess_input
from keras.models import load_model, Model
from keras.callbacks import ModelCheckpoint
import gc
from keras import backend as K
from keras.layers import LSTM, Embedding, TimeDistributed, Dense, RepeatVector, merge, Activation, Flatten, GRU, Input

class CaptionGenerator:

    def __init__(self):
        self.max_cap_len = None
        self.vocab_size = None
        self.index_word = None
        self.word_index = None
        self.total_samples = None
        #self.features = pickle.load(open('image_features.p', 'rb'))
        #self.data = pd.read_csv('data/flickr8k_train.csv', delimiter='\t')

        self.initValues()

    """
    loadCaptions()

    Load all descriptions from folder for each image.
    """
    def loadCaptions(self):
        files = os.listdir("descriptions")
        d = {'caption': [], 'image': []}
        for filename in files:
            f_ = filename.split('.')
            with open("descriptions/" + filename) as f:
                content = f.readlines()
            for text in content:
                d['caption'].append('<start> '+text.strip()+' <end>')
                d['image'].append(f_[0]+".jpg")
        self.data = pd.DataFrame(d)

    """
    processImages()

    Process images from table on VGG-16 model.
    """
    def processImages(self):
        imgs = self.data['image']
        print len(imgs)
        if len(imgs) > 0:
            model = VGG16(weights='imagenet', include_top=True, input_shape = (224, 224, 3))

            imgs = self.data['image']
            c = 0
            img_features = {}
            for img in imgs:
                c += 1
                img_feature = self.processImage(model, img)
                #img_feature = np.asarray(img_feature)
                #img_feature = img_feature.argmax(axis=-1)
                if c % 100 == 0:
                    print "Processed {0} images".format(c)
                img_features[img] = img_feature[0]
            self.features = img_features
            K.clear_session()

    """
    processImage(model, filename)

    Given model and filename, process an image on that filename over model.
    """
    def processImage(self, model, filename):
        img_dir = "images/"
        img_s = image.load_img(img_dir + filename, target_size=(224, 224))
        img_s = image.img_to_array(img_s)
        img_s = np.expand_dims(img_s, axis=0)
        img_s = preprocess_input(img_s)
        img_s = np.asarray(img_s)
        img_feature = model.predict(img_s)
        return img_feature

    """
    initValues()

    Init all parameters for generator.
    """
    def initValues(self):
        self.loadCaptions()
        self.processImages()
        self.total_samples=0
        for i, text in self.data.iterrows():
            self.total_samples+=len(text['caption'].split())-1
        print "Total samples : "+str(self.total_samples)

        # Split captions into words
        words = [txt['caption'].split() for i, txt in self.data.iterrows()]
        unique = []
        for word in words:
            unique.extend(word)

        # Genrate Vocabulary and index lists
        unique = list(set(unique))
        self.vocab_size = len(unique)
        self.word_index = {}
        self.index_word = {}
        for i, word in enumerate(unique):
            self.word_index[word]=i
            self.index_word[i]=word

        # Get max length of captions
        max_len = 0
        for i, caption in self.data.iterrows():
            if(len(caption['caption'].split()) > max_len):
                max_len = len(caption['caption'].split())
        self.max_cap_len = max_len
        print "Vocabulary size: "+str(self.vocab_size)
        print "Maximum caption length: "+str(self.max_cap_len)

        self.images = []
        for i, caption in self.data.iterrows():
            self.images.append(self.features[caption['image']])
        print "Image count: "+str(len(self.images))


    """
    generate(batch_size)

    Generate batches of prepared captions and images for training model.
    """
    def generate(self, batch_size=32):
        partial_caps = []
        next_words = []
        imgs = []
        print "Generating data..."
        gen_count = 0

        total_count = 1
        while 1:
            image_counter = -1
            for i, text in self.data.iterrows():
                image_counter += 1
                current_image = self.images[image_counter]
                for i in range(len(text['caption'].split())-1):
                    # Encode caption into index of words, removing last word.
                    total_count += 1
                    partial = [self.word_index[txt] for txt in text['caption'].split()[:i+1]]
                    partial_caps.append(partial)
                    # Get last word as next word and encode it
                    next = np.zeros(self.vocab_size)
                    next[self.word_index[text['caption'].split()[i+1]]] = 1
                    next_words.append(next)
                    imgs.append(current_image)
                    # Prepare inputs and return pair for batch
                    if total_count >= batch_size:
                        next_words = np.asarray(next_words)
                        imgs = np.asarray(imgs)
                        partial_caps = sequence.pad_sequences(partial_caps, maxlen=self.max_cap_len, padding='post')
                        total_count = 0
                        gen_count += 1
                        #print "yielding count: " + str(gen_count)
                        yield [[imgs, partial_caps], next_words]
                        partial_caps = []
                        imgs = []
                        next_words = []

    """
    train()

    Train a model.
    """
    def train(self):
        #Constructing a new model
        embedding_dim = 128
        image_input = Input(shape=(1000,))
        image_model = Dense(embedding_dim, input_dim=1000, activation='relu')(image_input)

        image_model = RepeatVector(self.max_cap_len)(image_model)

        language_input = Input(shape=(self.max_cap_len,))
        language_model = Embedding(self.vocab_size, 256, input_length=self.max_cap_len)(language_input)
        language_model = LSTM(256, return_sequences=True)(language_model)
        #language_model.add(GRU(128, return_sequences=True))
        language_model = TimeDistributed(Dense(embedding_dim))(language_model)

        output = merge([image_model, language_model], mode='concat')
        output = LSTM(1000, return_sequences=False)(output)
        #model.add(GRU(256, return_sequences=False))
        output = Dense(self.vocab_size)(output)
        output = Activation('softmax')(output)

        self.model = Model(inputs=[image_input, language_input], outputs=[output])

        self.model.compile(loss='categorical_crossentropy', optimizer='rmsprop', metrics=['accuracy'])

        #Train that model
        file_name = 'weights-improvement-{epoch:02d}.hdf5'
        checkpoint = ModelCheckpoint(file_name, monitor='loss', verbose=1, save_best_only=True, mode='min')
        callbacks_list = [checkpoint]
        self.model.fit_generator(self.generate(batch_size=1), steps_per_epoch=self.total_samples/1, epochs=20, verbose=1, callbacks=callbacks_list)
        try:
            self.model.save('Models/WholeModel.h5', overwrite=True)
            self.model.save_weights('Models/Weights.h5',overwrite=True)
            K.clear_session()
        except:
            print "Error in saving model."

    """
    generateCaption(filename)

    Generate a caption for a image, given a image filename.
    """
    def generateCaption(self, filename):
        image_feature = self.processImage(VGG16(weights='imagenet', include_top=True, input_shape = (224, 224, 3)), filename)[0]
        K.clear_session()
        self.model = load_model('Models/WholeModel.h5')
        #Give only start to prediction
        start = [self.word_index['<start>']]
        captions = [[start, 0.0]]
        #Predict and predict word after word on caption using words already predicted
        while(len(captions[0][0]) < self.max_cap_len):
            temp_captions = []
            for caption in captions:
                partial_caption = sequence.pad_sequences([caption[0]], maxlen=self.max_cap_len, padding='post')
                next_words_pred = self.model.predict([np.asarray([image_feature]), np.asarray(partial_caption)])[0]
                next_words = np.argsort(next_words_pred)[-3:]
                for word in next_words:
                    new_partial_caption, new_partial_caption_prob = caption[0][:], caption[1]
                    new_partial_caption.append(word)
                    new_partial_caption_prob+=next_words_pred[word]
                    temp_captions.append([new_partial_caption,new_partial_caption_prob])

            captions = temp_captions
            captions.sort(key = lambda l:l[1])
            captions = captions[-3:]

        #Resort and decode caption
        captions.sort(key = lambda l:l[1])
        best_caption = captions[-1][0]
        caption = " ".join([self.index_word[index] for index in best_caption])

        caption_split = caption.split()
        processed_caption = caption_split[1:]
        K.clear_session()
        try:
            end_index = processed_caption.index('<end>')
            processed_caption = processed_caption[:end_index]
        except:
            pass
        return " ".join([word for word in processed_caption])
