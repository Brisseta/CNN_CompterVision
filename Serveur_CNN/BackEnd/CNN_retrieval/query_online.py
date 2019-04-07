# -*- coding: utf-8 -*-
"""
@author:HuangJie
@time:18-9-17 下午2:48

"""
import threading

import h5py
import numpy as np

from BackEnd.Conf import Config
from BackEnd.Result import ResultFormat

'''
ap = argparse.ArgumentParser()
ap.add_argument("-query", required=True, help="Path to query which contains image to be queried")
ap.add_argument("-index", required=True, help="Path to index")
ap.add_argument("-result", required=True, help="Path for output retrieved images")
args = vars(ap.parse_args())
'''


class CNNQueryOnline(threading.Thread):

    def __init__(self, imagename, url, *args, **kwargs):
        self.imagename = imagename
        self.url = url
        super(CNNQueryOnline, self).__init__(*args, **kwargs)

    def CNNRequest(self):
        # read in indexed images' feature vectors and corresponding image names
        # h5f = h5py.File(args["index"], 'r')
        h5f = h5py.File(Config.H5F_LOCATION, 'r')
        feats = h5f['dataset_feat'][:]
        imgNames = h5f['dataset_name'][:]
        h5f.close()

        print("--------------------------------------------------")
        print("               searching starts")
        print("--------------------------------------------------")

        # read and show query image
        queryDir = Config.QUERY_DIRECTORY + self.imagename
        from BackEnd.CNN_retrieval.extract_cnn_vgg16_keras import VGGNet
        # init VGGNet16 model
        model = VGGNet()

        # extract query image's feature, compute simlarity score and sort
        queryVec = model.extract_feat(queryDir)
        scores = np.dot(queryVec, feats.T)
        rank_ID = np.argsort(scores)[::-1]
        rank_score = scores[rank_ID]
        # print rank_ID
        # print rank_score

        # number of top retrieved images to show
        result = []
        for i, index in enumerate(rank_ID[0:Config.MAX_RES]):
            result.append(ResultFormat(scores[index], str(imgNames[index])))
            print("score : %f , Url : %s" % (scores[index], str(imgNames[index])))

        return result
