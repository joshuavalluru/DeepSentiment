import numpy as np
import tflearn
import datetime

# Preprocessing function
def preprocess(data, columns_to_ignore):
    # Sort by descending id and delete columns
    for id in sorted(columns_to_ignore, reverse=True):
        [r.pop(id) for r in data]
    return np.array(data, dtype=np.float32)

ignore_columns = [0]
number_of_words = 3

from tflearn.data_utils import load_csv
data, labels = load_csv('../data/training/training.csv', categorical_labels=True, n_classes=2)
data = preprocess(data, ignore_columns)

# Build neural network
net = tflearn.input_data(shape=[None, 3])
net = tflearn.fully_connected(net, 32)
net = tflearn.fully_connected(net, 32)
net = tflearn.fully_connected(net, 2, activation='softmax')
net = tflearn.regression(net)


# Define model
model = tflearn.DNN(net)
# Start training (apply gradient descent algorithm)
model.fit(data, labels, n_epoch=10, batch_size=16, show_metric=True)

testdata, testlabels = load_csv('../data/training/test.csv', categorical_labels=True, n_classes=2)
testdata = preprocess(testdata, ignore_columns)

pred = model.predict(testdata)
hits = 0

for index, line in enumerate(pred):
    line[1] *= 2
    prediction = np.argmax(line)
    reality = testlabels[index][1]
    if prediction == reality:
	hits += 1
    print('%s :  %s' % (prediction, reality)) 

print ('Accuracy: %s' % (float(hits)/len(pred)))

model.save('../data/models/model-' + str(datetime.datetime.now()) + '.tflearn')
