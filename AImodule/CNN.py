# from konlpy.tag import Okt
from keras.preprocessing.text import Tokenizer
from tensorflow.keras.preprocessing.sequence import pad_sequences
from tensorflow.keras.models import load_model
import pickle
import re


save_model = 'C:\\Users\\samsung\\Desktop\\AI_backend_test\\myAI\\AImodule\\bilstm_best_model.h5'
save_tokenizer = 'C:\\Users\\samsung\\Desktop\\AI_backend_test\myAI\AImodule\\tokenizer.pickle'
loaded_model = load_model(save_model)

stopwords = ['도', '는', '다', '의', '가', '이', '은', '한', '에', '하', '고', '을', '를', '인', '듯', '과', '와', '네', '들', '듯', '지', '임', '게', '만', '게임', '겜', '되', '음', '면']
tmp = ['히히', '땡땡땡', '땡땡', '땡']
stopwords = stopwords + tmp

# # 형태소 분석기 Mecab을 사용하여 토큰화 작업을 수행
# okt = Okt()

max_len = 200

with open(save_tokenizer, 'rb') as handle:
    tokenizer = pickle.load(handle)

def VP_predict(sentence):
    # 입력받은 문장 전처리
    sentence = sentence = re.sub(r'[^ㄱ-ㅎㅏ-ㅣ가-힣 ]','', sentence)
    # 입력받은 문장 토큰화
    sentence = tokenizer.texts_to_sequences([sentence])
    # 토큰화된 문장 패딩 처리
    sentence = pad_sequences(sentence, maxlen=max_len)
    # 예측
    score = loaded_model.predict(sentence)
    # 예측 결과에 따라 레이블 결정
    if score >= 0.5:
        label = '피싱 대화'
        prob = score[0][0] * 100
    else:
        label = '일상 대화'
        prob = (1 - score[0][0]) * 100
    # 결과 출력
    print("%.2f%% 확률로 %s입니다." % (prob, label))