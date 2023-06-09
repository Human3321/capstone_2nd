from django.urls import path
from .views import VPpredictAPI

urlpatterns = [
    path('<int:num>/<str:txt>/', VPpredictAPI),
]

