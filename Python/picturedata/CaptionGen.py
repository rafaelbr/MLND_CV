import pandas as pd
import numpy as np
import cv2
import pickle
import os
from keras.preprocessing import image, sequence
from keras.applications.vgg16 import VGG16, preprocess_input
from keras.models import load_model
from keras.callbacks import ModelCheckpoint
import gc
from keras import backend as K

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

    def loadCaptions(self):
        files = os.listdir("descriptions")
        d = {'caption': [], 'image': []}
        for filename in files:
            f_ = filename.split('.')
            with open(filename) as f:
                content = f.readlines()
            for text in content:
                d['caption'].append(x.strip())
                d['image'].append(filename)
        self.data = pd.DataFrame(d)

    def processImages(self):
        imgs = self.data['image']
        if len(imgs) > 0:
            model = VGG16(weights='imagenet', include_top=True, input_shape = (224, 224, 3))
            img_dir = "images/"
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

    def processImage(model, filename):
        img_s = image.load_img(img_dir + filename, target_size=(224, 224))
        img_s = image.img_to_array(img_s)
        img_s = np.expand_dims(img_s, axis=0)
        img_s = preprocess_input(img_s)
        img_s = np.asarray(img_s)
        img_feature = model.predict(img_s)
        return img_feature

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
                    next[self.word_index[text[1].split()[i+1]]] = 1
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

    def train(self):
        self.model = load_model('Models/WholeModel.h5')
        self.model.compile(loss='categorical_crossentropy', optimizer='rmsprop', metrics=['accuracy'])
        file_name = 'weights-improvement-{epoch:02d}.hdf5'
        checkpoint = ModelCheckpoint(file_name, monitor='loss', verbose=1, save_best_only=True, mode='min')
        callbacks_list = [checkpoint]
        self.model.fit_generator(self.generate(batch_size=32), steps_per_epoch=self.total_samples/32, epochs=20, verbose=1, callbacks=callbacks_list)
        try:
            self.model.save('Models/WholeModel.h5', overwrite=True)
            self.model.save_weights('Models/Weights.h5',overwrite=True)
            K.clear_session()
        except:
            print "Error in saving model."

    def generateCaption(filename):
        image_feature = self.processImage(VGG16(weights='imagenet', include_top=True, input_shape = (224, 224, 3)), filename)
        K.clear_session()
        self.model = load_model('Models/WholeModel.h5')

        start = [self.word_index['<start>']]
        captions = [[start, 0.0]]
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
