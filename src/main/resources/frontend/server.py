# from flask import Flask, request
# from pymongo import MongoClient
# import datetime
# import time
#
#
# client = MongoClient(
#     "mongodb://fdse:fdse@10.176.34.90:27017/fas")
# db = client['fas']
# collection = db['test_order']
#
# app = Flask(__name__)
#
#
# @app.route('/upload/dsl', methods=['POST'])
# def index():
#     data = request.get_json()
#
#     now = datetime.datetime.now()
#     start_timestamp = data["startTime"]
#     timestamp = datetime.datetime.timestamp(now)*1000
#     time_difference = timestamp - start_timestamp
#     data["duration"] = str(round(time_difference/1000/60, 2)) + "min"
#     data["endTime"] = now.strftime('%Y-%m-%d %H:%M:%S')
#     data.pop("startTime")
#     collection.insert_one(data)
#     return {"code": 200, "msg": "success"}
#
#
# if __name__ == '__main__':
#     app.run(host='0.0.0.0', port=8000)
