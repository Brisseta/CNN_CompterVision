import threading

import cv2
from pylab import *
from scipy.cluster.vq import *
from sklearn import preprocessing
from sklearn.externals import joblib

from BackEnd.Conf import Config
from BackEnd.Result import ResultFormat


class BOWQueryOnline(threading.Thread):
    def __init__(self, imagename, url, *args, **kwargs):
        self.imagename = imagename
        self.url = url
        super(BOWQueryOnline, self).__init__(*args, **kwargs)

    def Bowrequest(self):
        # Get query image path

        # Load the classifier, class names, scaler, number of clusters and vocabulary
        im_features, image_paths, idf, numWords, voc = joblib.load(Config.PKL_LOCATION)

        # Create feature extraction and keypoint detector objects
        # List where all the descriptors are stored
        des_list = []
        sift = cv2.xfeatures2d.SIFT_create()

        im = cv2.imread(self.imagename)
        gray = cv2.cvtColor(im, cv2.COLOR_BGR2GRAY)
        kp, des = sift.detectAndCompute(im, None)

        des_list.append((self.imagename, des))

        # Stack all the descriptors vertically in a numpy array
        descriptors = des_list[0][1]

        #
        test_features = np.zeros((1, numWords), "float32")
        words, distance = vq(descriptors, voc)
        for w in words:
            test_features[0][w] += 1

        # Perform Tf-Idf vectorization and L2 normalization
        test_features = test_features * idf
        test_features = preprocessing.normalize(test_features, norm='l2')

        score = np.dot(test_features, im_features.T)
        rank_ID = np.argsort(-score)

        # Visualize the results
        figure()
        # gray()
        subplot(5, 4, 1)
        imshow(im[:, :, ::-1])
        axis('off')
        result = []
        for i, ID in enumerate(rank_ID[0][0:16]):
            result.append(ResultFormat(score[ID], image_paths[ID]))
            # img = Image.open(image_paths[ID])
            # # gray()
            # subplot(5, 4, i + 5)
            # imshow(img)
            # axis('off')

        # show()
        return result[:3]
