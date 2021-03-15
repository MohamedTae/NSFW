import cv2
from threading import Thread
from functools import partial
import os


number_of_scripts = 6
video_name = '1.mp4'

# create video capture object
data = cv2.VideoCapture(video_name)

# count the number of frames 
frames = data.get(cv2.CAP_PROP_FRAME_COUNT)
fps = int(data.get(cv2.CAP_PROP_FPS))

# calculate dusration of the video
seconds = int(frames / fps)
print("duration in seconds:", seconds)
duration = int(seconds / number_of_scripts)
print(duration)


w1 = os.system("python C:/Users/DevOps/PycharmProjects/AI/nsfw.py "+str('0')+" "+str(duration)+" "+"'"+video_name+"'")
w2 = os.system("python C:/Users/DevOps/PycharmProjects/AI/nsfw.py "+str(duration)+" "+str(duration*2)+" "+"'"+video_name+"'")
w3 = os.system("python C:/Users/DevOps/PycharmProjects/AI/nsfw.py "+str(duration*2)+" "+str(duration*3)+" "+"'"+video_name+"'")
w4 = os.system("python C:/Users/DevOps/PycharmProjects/AI/nsfw.py "+str(duration*3)+" "+str(duration*4)+" "+"'"+video_name+"'")
w5 = os.system("python C:/Users/DevOps/PycharmProjects/AI/nsfw.py "+str(duration*4)+" "+str(duration*5)+" "+"'"+video_name+"'")
w6 = os.system("python C:/Users/DevOps/PycharmProjects/AI/nsfw.py "+str(duration*5)+" "+str(duration*6)+" "+"'"+video_name+"'")

print(w1)