import pandas as pd
import numpy as np
import cv2
import pickle
from keras.preprocessing import image, sequence

class DataGenerator:

    def __init__(self):
        self.max_cap_len = None
        self.vocab_size = None
        self.index_word = None
        self.word_index = None
        self.total_samples = None
        self.features = pickle.load(open('image_features.p', 'rb'))
        self.data = pd.read_csv('data/flickr8k_train.csv', delimiter='\t')

        self.initValues()

    def initValues(self):

        self.total_samples=0
        for i, text in self.data.iterrows():
            self.total_samples+=len(text['caption'].split())-1
        print "Total samples : "+str(self.total_samples)

        words = [txt['caption'].split() for i, txt in self.data.iterrows()]
        unique = []
        for word in words:
            unique.extend(word)

        unique = list(set(unique))
        self.vocab_size = len(unique)
        self.word_index = {}
        self.index_word = {}
        for i, word in enumerate(unique):
            self.word_index[word]=i
            self.index_word[i]=word

        max_len = 0
        for i, caption in self.data.iterrows():
            if(len(caption['caption'].split()) > max_len):
                max_len = len(caption['caption'].split())
        self.max_cap_len = max_len
        print "Vocabulary size: "+str(self.vocab_size)
        print "Maximum caption length: "+str(self.max_cap_len)

        self.images = []
        for i, caption in self.data.iterrows():
            self.images.append(self.data[caption['image']])

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
                    total_count += 1
                    partial = [self.word_index[txt] for txt in text['caption'].split()[:i+1]]
                    partial_caps.append(partial)
                    next = np.zeros(self.vocab_size)
                    next[self.word_index[text[1].split()[i+1]]] = 1
                    next_words.append(next)
                    imgs.append(current_image)

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

    def getMaxCapLen(self):
        return self.max_cap_len

    def getVocabSize(self):
        return self.vocab_size
