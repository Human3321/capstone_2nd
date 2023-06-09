from konlpy.tag import Okt
from keras.preprocessing.text import Tokenizer
from tensorflow.keras.preprocessing.sequence import pad_sequences
from tensorflow.keras.models import load_model
import pickle
import re


save_model = 'C:\Users\samsung\Desktop\AI_backend_test\myAI\AImodule\bilstm_best_model.h5'
save_tokenizer = 'C:\Users\samsung\Desktop\AI_backend_test\myAI\AImodule\tokenizer.pickle'
loaded_model = load_model(save_model)

stopwords = ['도', '는', '다', '의', '가', '이', '은', '한', '에', '하', '고', '을', '를', '인', '듯', '과', '와', '네', '들', '듯', '지', '임', '게', '만', '게임', '겜', '되', '음', '면']
tmp = ['히히', '땡땡땡', '땡땡', '땡']
stopwords = stopwords + tmp

# 형태소 분석기 Okt 사용하여 토큰화 작업을 수행
okt = Okt()

max_len = 200

with open(save_tokenizer, 'rb') as handle:
    tokenizer = pickle.load(handle)

def VP_predict(new_sentence):
    new_sentence_data = re.sub(r'[^ㄱ-ㅎㅏ-ㅣ가-힣 ]','', new_sentence)
    new_sentence_data = re.sub('[-=+,#/\?:^.@*\"※~ㆍ!』‘|\(\)\[\]`\'…》\”\“\’·]', ' ', new_sentence_data)
    new_sentence_data = okt.morphs(new_sentence_data) # 토큰화
    new_sentence_data = [word for word in new_sentence_data if not word in stopwords] # 불용어 제거
    if(len(new_sentence_data) < 40):
        new_sentence_data = new_sentence_data * 8
    #print("길이 : ", len(new_sentence_data))
    encoded = tokenizer.texts_to_sequences([new_sentence_data]) # 정수 인코딩
    pad_new = pad_sequences(encoded, maxlen = max_len) # 패딩
    score = float(loaded_model.predict(pad_new, verbose=0)) # 예측
    #print("{}% 확률".format(score * 100))
    score = round(score, 4) * 100
    if(score > 50.5):
        return [score, 1]
    else:
        return [score, 0]
