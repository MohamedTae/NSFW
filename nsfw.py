from nudenet import NudeClassifier
import cv2
import openpyxl
import sys


sec = int(sys.argv[1])
max = int(sys.argv[2])
video_name = str(sys.argv[3])

def Check(sec,max,video_name):
    wb = openpyxl.load_workbook(filename='C:/Users/DevOps/PycharmProjects/AI/test.xlsx', data_only=True)
    kk = wb.active

    classifier = NudeClassifier()
    vidcap = cv2.VideoCapture(video_name)


    def getFrame(sec):
        vidcap.set(cv2.CAP_PROP_POS_MSEC,sec*1000)
        hasFrames,image = vidcap.read()
        if hasFrames:
            cv2.imwrite("1/frame "+str(sec)+" sec.jpg", image)     # save frame as JPG file
            info_dict = classifier.classify("1/frame "+str(sec)+" sec.jpg")
            if info_dict["1/frame "+str(sec)+" sec.jpg"]['unsafe'] >= 0.99:
                print(info_dict)
                row = (int(sec),"")
                kk.append(row)
        return hasFrames

    frameRate = 1
    success = getFrame(sec)
    while sec < max:
        sec = sec + frameRate
        sec = round(sec, 2)
        success = getFrame(sec)

    wb.save('Result.xlsx')


Check(sec,max,video_name)
