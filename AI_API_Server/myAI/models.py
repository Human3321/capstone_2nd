from django.db import models

class MyAiModel(models.Model):
    number = models.IntegerField()
    body = models.TextField()
    VPpercent = models.FloatField()
    answer = models.IntegerField()