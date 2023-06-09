from rest_framework.response import Response
from rest_framework.decorators import api_view
from .models import MyAiModel
from django.http import JsonResponse
from .AImodule.BILSTM import VP_predict

    
@api_view(['GET'])
def VPpredictAPI(request, num, txt) :
    try :
        total = MyAiModel.objects.all()
        total.number = num
        total.body = txt.replace("+"," ")
        total.VPpercent, total.answer = VP_predict(total.body) # Ai.VP_predict(txt)
        print("대화\n:", total.body)
        print("보이스피싱 확률\n:", total.VPpercent,'%')
        print("보이스피싱 판별 결과\n: ",total.answer)
        response = {'percent': total.VPpercent , 'answer':total.answer}
        return JsonResponse(response, status=200)
    except :
        response = {'percent': 0 , 'answer':0}
        print("message: Invalid request method 400")
        return JsonResponse(response, status=400)



