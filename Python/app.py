#!flask/bin/python
from flask import Flask, send_from_directory
from picturedata import PictureData

app = Flask(__name__)

picturedata = PictureData()

@app.route("/")
def index():
    return "Hello world!"

@app.route("/images/<path:path>")
def send_images(path):
    return send_from_directory('images', path)

@app.route("/socialpicture/api/v1.0/images", methods=['GET'])
def get_images():
    return picturedata.get_data()

@app.route("/socialpicture/api/v1.0/images/<int:image_id>", methods=['GET'])
def get_image(image_id):
    return picturedata.get_image(image_id)

@app.route("/socialpicture/api/v1.0/images", methods=['POST'])
def new_image():
    return picturedata.new_image()

@app.route("/socialpicture/api/v1.0/images/<int:image_id>", methods=['PUT'])
def add_description(image_id):
    return picturedata.add_description(image_id)

if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0')
