#!usr/bin/python
from flask import jsonify, abort, request
import json
import os

class PictureData:
    data = []

    def __init__(self):
        #load data from filesystem
        files = os.listdir('images')
        for f in files:
            file_data = f.split('.')
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
        index = self.data[-1]['id'] + 1
        with open('images/{0}.{1}'.format(index, request.json.get('extension')), 'wb') as fh:
            fh.write(image_data.decode('base64'))
        image = {
            'id': self.data[-1]['id'] + 1,
            'extension': request.json.get('extension'),
            'image': 'images/{0}.{1}'.format(index, request.json.get('extension')),
            'description': ''
        }
        self.data.append(image)
        return jsonify(image), 201

    def add_description(self, image_id):
        if not request.json or not 'description' in request.json:
            abort(400)
        with open('descriptions/{0}.txt'.format(self.data[-1]['id'] + 1), 'a') as fh:
            fh.write('{0}\n'.format(request.json.get('description')))
        return jsonify({'result': 'Description added'})
