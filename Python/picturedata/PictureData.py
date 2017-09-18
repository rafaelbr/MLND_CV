#!usr/bin/python
from flask import jsonify, abort, request
from CaptionGen import CaptionGenerator
import json
import os
import threading

class Singleton(type):
    _instances = {}
    def __call__(cls, *args, **kwargs):
        if cls not in cls._instances:
            cls._instances[cls] = super(Singleton, cls).__call__(*args, **kwargs)
        return cls._instances[cls]

class PictureData:
    __metaclass__ = Singleton
    data = []

    def __init__(self):
        self.processData()
        #t = threading.Timer(7200, self.processData)
        #t.start()

    def processData(self):
        self.data = []
        generator = CaptionGenerator()
        files = os.listdir('images')
        print len(files)
        print len(os.listdir('descriptions'))
        if len(files) > 0 and len(os.listdir('descriptions')) > 0:
            generator.train()
        #load data from filesystem

        for f in files:
            file_data = f.split('.')
            files_desc = os.listdir('descriptions')
            hasDesc = False
            for f_desc in files_desc:
                data = f_desc.split('.')
                if data[0] == file_data[0]:
                    hasDesc = True
                    break;
            if hasDesc and len(files) > 0:
                image = {
                    'id': file_data[0],
                    'image': '/images/{0}'.format(f),
                    'extension': file_data[1],
                    'description': generator.generateCaption(f)
                    }
            else:
                image = {
                    'id': file_data[0],
                    'image': '/images/{0}'.format(f),
                    'extension': file_data[1],
                    'description': ''
                    }

            self.data.append(image)


    def get_data(self):
        return json.dumps(self.data)

    def get_image(self, image_id):
        image = [image for image in self.data if image['id'] == image_id]
        if len(image) == 0:
            abort(404)
        return jsonify(image[0])

    def new_image(self):
        if not request.json or not 'image' in request.json:
            abort(400)

        image_data = request.json.get('image')
        #save image
        index = 0
        if len(self.data) > 0:
            index = self.data[-1]['id'] + 1
        else:
            index = 1
        with open('images/{0}.{1}'.format(index, request.json.get('extension')), 'wb') as fh:
            fh.write(image_data.decode('base64'))
        image = {
            'id': index,
            'extension': request.json.get('extension'),
            'image': 'images/{0}.{1}'.format(index, request.json.get('extension')),
            'description': ''
        }
        self.data.append(image)
        return jsonify(image), 201

    def add_description(self, image_id):
        if not request.json or not 'description' in request.json:
            abort(400)
        index = 0
        with open('descriptions/{0}.txt'.format(image_id), 'a') as fh:
            fh.write('{0}\n'.format(request.json.get('description')))
        return jsonify({'result': 'Description added'})
