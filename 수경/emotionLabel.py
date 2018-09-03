from __future__ import print_function
import time 
import requests
import cv2
import operator
import numpy as np
import sys
import os
import shutil

# Import library to display results
import matplotlib.pyplot as plt
#%matplotlib inline 
# Display images within Jupyter

# Variables

_url = 'https://eastasia.api.cognitive.microsoft.com/face/v1.0/detect'
_key = '5257cadfccb347eab47a0f95cafd85aa' #Here you have to paste your primary key
_maxNumRetries = 10

def processRequest( json, data, headers, params ):

    """
    Helper function to process the request to Project Oxford

    Parameters:
    json: Used when processing images from its URL. See API Documentation
    data: Used when processing image read from disk. See API Documentation
    headers: Used to pass the key information and the data type request
    """

    retries = 0
    result = None

    while True:

        response = requests.request( 'post', _url, json = json, data = data, headers = headers, params = params )

        if response.status_code == 429: 

            print( "Message: %s" % ( response.json()['error']['message'] ) )

            if retries <= _maxNumRetries: 
                time.sleep(1) 
                retries += 1
                continue
            else: 
                print( 'Error: failed after retrying!' )
                break

        elif response.status_code == 200 or response.status_code == 201:

            if 'content-length' in response.headers and int(response.headers['content-length']) == 0: 
                result = None 
            elif 'content-type' in response.headers and isinstance(response.headers['content-type'], str): 
                if 'application/json' in response.headers['content-type'].lower(): 
                    result = response.json() if response.content else None 
                elif 'image' in response.headers['content-type'].lower(): 
                    result = response.content
        else:
            print( "Error code: %d" % ( response.status_code ) )
            print( "Message: %s" % ( response.json()['error']['message'] ) )

        break
        
    return result

def renderResultOnImage( result, img ):
    
    """Display the obtained results onto the input image"""
    
    for currFace in result:
        faceRectangle = currFace['faceRectangle']
        cv2.rectangle( img,(faceRectangle['left'],faceRectangle['top']),
                           (faceRectangle['left']+faceRectangle['width'], faceRectangle['top'] + faceRectangle['height']),
                       color = (255,0,0), thickness = 5 )


    for currFace in result:
        faceRectangle = currFace['faceRectangle']
        faceAttributes=currFace['faceAttributes']
        listvalue=list(currFace['faceAttributes'].values())
        
        emotion = max(listvalue[0].items(), key=operator.itemgetter(1))[0]
        #print(listvalue[0].values())
        #print(listvalue[0].keys())
        print (emotion)
        return emotion

# Request parameters.
params = {
 
    'returnFaceId': 'true',
 
    'returnFaceLandmarks': 'false',
 
    'returnFaceAttributes': 'emotion',
 
}
def getIndex(emotion):
    return {"neutral": 0,
                "happiness":1,
                "anger":2,
                "contempt":3,
                "disgust":4,
                "fear":5,
                "sadness":6,
                "surprise":7
                }.get(emotion, "No data")

def main(argv):
    srcdir = argv[1]
    desdir = argv[2]
    
    file_list = os.listdir(srcdir)
    #file_list.sort()
    x=0

    headers = dict()
    headers['Ocp-Apim-Subscription-Key'] = _key
    headers['Content-Type'] = 'application/octet-stream'

    json = None

    for item in file_list:
        #if x== 10 :
          #  break
        #else:
            pathToFileInDisk = srcdir+"\\"+item
            with open(pathToFileInDisk,'rb') as f:
                data = f.read()

            result = processRequest( json, data, headers, params )

            if result is not None:
                # Load the original image from disk
                data8uint = np. frombuffer ( data, np.uint8 ) # Convert string to an unsigned int array
                img = cv2.cvtColor( cv2.imdecode( data8uint, cv2.IMREAD_COLOR ), cv2.COLOR_BGR2RGB )

                emotion= renderResultOnImage( result, img )
                #dirEmotion = desdir + "\\" + emotion
                dirEmotion = desdir+"\\emotion"
                if not os.path.isdir(dirEmotion) :
                    os.mkdir(dirEmotion)
                textFile = open(dirEmotion+"\\emotion.txt",'a')
                shutil.move(pathToFileInDisk, dirEmotion)
                
                textFile.write("%s,%s,%d\n"%(dirEmotion+"\\"+item,emotion,getIndex(emotion)))
            #x=x+1
    textFile.close()

if __name__=="__main__":
    main(sys.argv)
