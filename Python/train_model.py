from picturedata.generator import DataGenerator
from keras.models import Model
from keras.layers import LSTM, Embedding, TimeDistributed, Dense, RepeatVector, merge, Activation, Flatten, GRU, Input
from keras.callbacks import ModelCheckpoint

generator = DataGenerator()

embedding_dim = 128
image_input = Input(shape=(1000,))
image_model = Dense(embedding_dim, input_dim=1000, activation='relu')(image_input)

image_model = RepeatVector(generator.getMaxCapLen())(image_model)

language_input = Input(shape=(generator.getMaxCapLen(),))
language_model = Embedding(generator.getVocabSize(), 256, input_length=generator.getMaxCapLen())(language_input)
language_model = LSTM(256, return_sequences=True)(language_model)
#language_model.add(GRU(128, return_sequences=True))
language_model = TimeDistributed(Dense(embedding_dim))(language_model)

output = merge([image_model, language_model], mode='concat')
output = LSTM(1000, return_sequences=False)(output)
#model.add(GRU(256, return_sequences=False))
output = Dense(generator.getVocabSize())(output)
output = Activation('softmax')(output)

model = Model(inputs=[image_input, language_input], outputs=[output])

model.compile(loss='categorical_crossentropy', optimizer='rmsprop', metrics=['accuracy'])

file_name = 'weights-improvement-{epoch:02d}.hdf5'
checkpoint = ModelCheckpoint(file_name, monitor='loss', verbose=1, save_best_only=True, mode='min')
callbacks_list = [checkpoint]
model.fit_generator(generator.generate(batch_size=32), steps_per_epoch=generator.total_samples/32, epochs=50, verbose=1, callbacks=callbacks_list)
try:
    model.save('Models/WholeModel.h5', overwrite=True)
    model.save_weights('Models/Weights.h5',overwrite=True)
except:
    print "Error in saving model."
