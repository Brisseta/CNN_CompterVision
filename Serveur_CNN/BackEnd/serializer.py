from rest_framework import serializers

from BackEnd.models import Url, ClientSearch


class UrlSerializer(serializers.ModelSerializer):
    class Meta:
        model = Url
        fields = ('id', 'result_lib', 'count', 'score')

    # def create(self, validated_data):
    #     return Url(**validated_data)
    #
    # def update(self, instance, validated_data):
    #     instance.url_lib = validated_data.get('url_lib', instance.url_lib)
    #     instance.count = validated_data.get('count', instance.count)
    #     instance.score = validated_data.get('score', instance.score)
    #     return instance


class PostSerializer(serializers.ModelSerializer):
    class Meta:
        model = ClientSearch
        fields = ('data_location',)


class SearchSerializer(serializers.ModelSerializer):
    urls = UrlSerializer(many=True)

    class Meta:
        model = ClientSearch
        fields = ('client_ip', 'post_date', 'data_location', 'urls')

    # def create(self, validated_data):
    #     return ClientSearch(**validated_data)

    # def update(self, instance, validated_data):
    #     instance.received = validated_data.get('received', instance.received)
    #     instance.analyzed = validated_data.get('analyzed', instance.analyzed)
    #     return instance
