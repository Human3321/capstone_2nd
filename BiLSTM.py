total_data = pd.read_csv("C:/Users/jinhe/Desktop/shsans/VP_dataset.csv",delimiter=',')
print('전체 리뷰 개수 :',len(total_data))
# 중복 제거
total_data.drop_duplicates(subset=['대화'], inplace=True)
print('총 샘플의 수 :',len(total_data))
# NULL 값 제거
total_data = total_data.dropna(axis=0)
# NULL 값 유무 확인
print(total_data.isnull().values.any())

print(total_data.groupby('VP 여부').size().reset_index(name = 'count'))

# 훈련 데이터와 텍스트 데이터 분류
train_data, test_data = train_test_split(total_data, test_size = 0.25, random_state = 42)
print('훈련용 리뷰의 개수 :', len(train_data))
print('테스트용 리뷰의 개수 :', len(test_data))

# 레이블 별 분포 확인
print(train_data.groupby('VP 여부').size().reset_index(name = 'count'))
print(test_data.groupby('VP 여부').size().reset_index(name = 'count'))

# 데이터 정제
# 한글과 공백을 제외하고 모두 제거
train_data['대화'] = train_data['대화'].str.replace("[^ㄱ-ㅎㅏ-ㅣ가-힣 ]","")
train_data['대화'].replace('', np.nan, inplace=True)
print(train_data.isnull().sum())

test_data.drop_duplicates(subset = ['대화'], inplace=True) # 중복 제거
test_data['대화'] = test_data['대화'].str.replace("[^ㄱ-ㅎㅏ-ㅣ가-힣 ]","") # 정규 표현식 수행
test_data['대화'].replace('', np.nan, inplace=True) # 공백은 Null 값으로 변경
test_data = test_data.dropna(how='any') # Null 값 제거
print('전처리 후 테스트용 샘플의 개수 :',len(test_data))

# 불용어 정의
stopwords = ['도', '는', '다', '의', '가', '이', '은', '한', '에', '하', '고', '을', '를', '인', '듯', '과', '와', '네', '들', '듯', '지', '임', '게', '만', '게임', '겜', '되', '음', '면']
tmp = ['히히', '땡땡땡', '땡땡', '땡']
stopwords = stopwords + tmp

# 토큰화 후 단어 길이 분포 확인
okt = Okt()

train_data['tokenized'] = train_data['대화'].apply(okt.morphs)
train_data['tokenized'] = train_data['tokenized'].apply(lambda x: [item for item in x if item not in stopwords])
test_data['tokenized'] = test_data['대화'].apply(okt.morphs)
test_data['tokenized'] = test_data['tokenized'].apply(lambda x: [item for item in x if item not in stopwords])

# 각 레이블에 따라서 별도로 단어들의 리스트를 저장
negative_words = np.hstack(train_data[train_data['VP 여부'] == 0]['tokenized'].values)
positive_words = np.hstack(train_data[train_data['VP 여부'] == 1]['tokenized'].values)

# 긍정 및 부정 단어 출력
negative_word_count = Counter(negative_words)
print(negative_word_count.most_common(20))
positive_word_count = Counter(positive_words)
print(positive_word_count.most_common(20))

fig,(ax1,ax2) = plt.subplots(1,2,figsize=(10,5))
text_len = train_data[train_data['VP 여부']==1]['tokenized'].map(lambda x: len(x))
ax1.hist(text_len, color='red')
ax1.set_title('Positive Reviews')
ax1.set_xlabel('length of samples')
ax1.set_ylabel('number of samples')
print('일상 대화의 평균 길이 :', np.mean(text_len))

text_len = train_data[train_data['VP 여부']==0]['tokenized'].map(lambda x: len(x))
ax2.hist(text_len, color='blue')
ax2.set_title('Negative Reviews')
fig.suptitle('Words in texts')
ax2.set_xlabel('length of samples')
ax2.set_ylabel('number of samples')
print('피싱 대화의 평균 길이 :', np.mean(text_len))
plt.show()

X_train = train_data['tokenized'].values
y_train = train_data['VP 여부'].values
X_test = test_data['tokenized'].values
y_test = test_data['VP 여부'].values

# 정수 인코딩
tokenizer = Tokenizer()
tokenizer.fit_on_texts(X_train)

threshold = 2
total_cnt = len(tokenizer.word_index) # 단어의 수
rare_cnt = 0 # 등장 빈도수가 threshold보다 작은 단어의 개수를 카운트
total_freq = 0 # 훈련 데이터의 전체 단어 빈도수 총 합
rare_freq = 0 # 등장 빈도수가 threshold보다 작은 단어의 등장 빈도수의 총 합

# 단어와 빈도수의 쌍(pair)을 key와 value로 받는다.
for key, value in tokenizer.word_counts.items():
    total_freq = total_freq + value

    # 단어의 등장 빈도수가 threshold보다 작으면
    if(value < threshold):
        rare_cnt = rare_cnt + 1
        rare_freq = rare_freq + value

print('단어 집합(vocabulary)의 크기 :',total_cnt)
print('등장 빈도가 %s번 이하인 희귀 단어의 수: %s'%(threshold - 1, rare_cnt))
print("단어 집합에서 희귀 단어의 비율:", (rare_cnt / total_cnt)*100)
print("전체 등장 빈도에서 희귀 단어 등장 빈도 비율:", (rare_freq / total_freq)*100)

# 전체 단어 개수 중 빈도수 2이하인 단어 개수는 제거.
# 0번 패딩 토큰과 1번 OOV 토큰을 고려하여 +2
vocab_size = total_cnt - rare_cnt + 2
print('단어 집합의 크기 :',vocab_size)

tokenizer = Tokenizer(vocab_size, oov_token = 'OOV') 
tokenizer.fit_on_texts(X_train)
X_train = tokenizer.texts_to_sequences(X_train)
X_test = tokenizer.texts_to_sequences(X_test)

import pickle
with open('tokenizer.pickle', 'wb') as handle:
    pickle.dump(tokenizer, handle)
    
print(X_train[:3])
print(X_test[:3])

print('대화의 최대 길이 :',max(len(review) for review in X_train))
print('대화의 평균 길이 :',sum(map(len, X_train))/len(X_train))
plt.hist([len(review) for review in X_train], bins=50)
plt.xlabel('length of samples')
plt.ylabel('number of samples')
plt.show()

def below_threshold_len(max_len, nested_list):
  count = 0
  for sentence in nested_list:
    if(len(sentence) <= max_len):
        count = count + 1
  print('전체 샘플 중 길이가 %s 이하인 샘플의 비율: %s'%(max_len, (count / len(nested_list))*100))
max_len = 150
below_threshold_len(max_len, X_train)

X_train = pad_sequences(X_train, maxlen=max_len)
X_test = pad_sequences(X_test, maxlen=max_len)

embedding_dim = 32
hidden_units = 128

model = Sequential()
model.add(Embedding(vocab_size, embedding_dim))
model.add(Bidirectional(LSTM(hidden_units))) # Bidirectional LSTM을 사용
model.add(Dense(1, activation='sigmoid'))

es = EarlyStopping(monitor='val_loss', mode='min', verbose=1, patience=4)
mc = ModelCheckpoint('bilstm_best_model.h5', monitor='val_acc', mode='max', verbose=1, save_best_only=True)

model.compile(optimizer='adam', loss='binary_crossentropy', metrics=['acc'])
model.summary()
history = model.fit(X_train, y_train, epochs=10, callbacks=[es, mc], batch_size=64, validation_split=0.2)

loaded_model = load_model('bilstm_best_model.h5')
print("테스트 정확도: %.4f" % (loaded_model.evaluate(X_test, y_test)[1]))

epochs = range(1, len(history.history['acc']) + 1)

flt, ax1 = plt.subplots()
ax1.set_ylabel('loss')
ax1.set_xlabel('epoch')

ax1.plot(epochs, history.history['loss'], 'r--', label = 'train_loss')
ax1.plot(epochs, history.history['val_loss'], 'g-', label='val_loss')

ax2 = ax1.twinx()
ax2.set_ylabel('Accuracy')
ax2.plot(epochs, history.history['val_acc'], 'b--', label='val_acc')

plt.title('BiLSTM model')
ax1.legend(['train_loss', 'val_loss'], loc='upper left')
ax2.legend(['val_acc'], loc='lower left')
plt.show()

def sentiment_predict(new_sentence):
  print(new_sentence)
  new_sentence = re.sub(r'[^ㄱ-ㅎㅏ-ㅣ가-힣 ]','', new_sentence) # 전처리
  new_sentence = okt.morphs(new_sentence) # 토큰화
  new_sentence = [word for word in new_sentence if not word in stopwords] # 불용어 제거
  encoded = tokenizer.texts_to_sequences([new_sentence]) # 정수 인코딩
  pad_new = pad_sequences(encoded, maxlen = max_len) # 패딩
  score = float(loaded_model.predict(pad_new)) # 예측
  if(score > 0.5):
    print("{:.2f}% 확률로 피싱 대화입니다.".format(score * 100))
  else:
    print("{:.2f}% 확률로 일상 대화입니다.".format((1 - score) * 100))

# 일상대화 테스트
sentiment_predict('어릴때 웨딩천사 피치봤어? 응 그거 봤지 나는 데일리 좋아했어 데일리가 단발머리 여자애지? 응 맞어 보이시해서 좋아했지 나는 릴리좋아했는데 릴리는 진짜 여자여자했어 요즘 그거 콜라보해서 화장품 만들었더라 나도 그거봤어 살뻔했잖아 우리시절 여자라면 그거 하나쯤 같고 싶었잖아 맞어  팩트처럼 생긴 거랑 요술봉')