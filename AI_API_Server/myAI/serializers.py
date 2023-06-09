from rest_framework import serializers
from .models import MyAiModel

class MyAiSerializer(serializers.ModelSerializer):
    class Meta:
        model = MyAiModel
        fields = ('number', 'body', 'VPpercent', 'answer')


