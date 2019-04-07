import base64

from django.core.files.base import ContentFile
from django.core.files.storage import default_storage
from django.utils import timezone
from rest_framework import status
from rest_framework.response import Response
from rest_framework.views import APIView

from BackEnd.Conf import Config
from BackEnd.models import ClientSearch, Url
from BackEnd.serializer import SearchSerializer, PostSerializer


# Create your views here.

class Index(APIView):

    def get(self, request, format=None):
        searches = ClientSearch.objects.all()
        serializer = SearchSerializer(searches, many=True)
        return Response(serializer.data, status=status.HTTP_200_OK)

    def post(self, request, format=None):
        # CONVERT B64 TO IMG#
        my_object = ClientSearch(client_ip=request.data['client_ip'], post_date=timezone.now(),
                                 data_location="/img_searches/" + str(ClientSearch.objects.last().id + 1) + "/")
        if request.data['data'].startswith("data"):
            format, imgstr = request.data['data'].split(';base64,')
            ext = format.split('/'[-1])
            image = ContentFile(base64.b64decode(imgstr), name='temp.' + my_object.data_location + "." + ext[1])
        else:
            image = ContentFile(base64.b64decode(request.data['data']), name='temp.' + my_object.data_location + ".jpg")

        # STORE TO OUR IMG DATABASE#

        path = default_storage.save(image.name, image)

        # BEGIN OF PROCESSING#
        if request.data['method'] == 'CNN':
            from BackEnd.CNN_retrieval.query_online import CNNQueryOnline
            cnnrequest = CNNQueryOnline(image.name, my_object.data_location)
            async_result = cnnrequest.CNNRequest()
        elif request.data['method'] == 'BOW':
            from BackEnd.bow_retrieval.onlin_search import BOWQueryOnline
            bowrequest = BOWQueryOnline(image.name, my_object.data_location)
            async_result = bowrequest.Bowrequest()
        else:
            async_result = ["no_data"]

        # SAVING SEARCH
        my_object.save()
        for i in range(len(async_result)):
            print(async_result[i])
            temp = str(async_result[i].url).replace("'", '').replace("b", "")
            print(temp)
            tempurl = Url(result_lib=Config.SERVER_STATIC + temp,
                          score=async_result[i].score,
                          search_id=ClientSearch.objects.last().id)
            #     TODO faire le formatage de l'url a stocker
            tempurl.save()

        return Response(PostSerializer(my_object).data, status=status.HTTP_201_CREATED)


class SearchDetail(APIView):
    def get(self, request, pk, format=None):
        search = ClientSearch.objects.get(id=pk)
        serializer = SearchSerializer(search, many=False)
        return Response(serializer.data, status=status.HTTP_200_OK)


class Indexing(APIView):
    def post(self, request, format=None):
        indexing_type = request.data['number']
        number_of_result = request.data['number']
        Config.MAX_RES = number_of_result
        if indexing_type in ['bow', 'CNN']:
            if indexing_type == 'CNN':
                from BackEnd.CNN_retrieval.index import CNNIndexing
                # pool = Pool(processes=1)
                # pool.apply_async(CNNIndexing.index, [10])
                result = CNNIndexing.index
                return Response("Indexing result : %d" % result, status=status.HTTP_201_CREATED)

        return Response("Bad Request", status=status.HTTP_400_BAD_REQUEST)
