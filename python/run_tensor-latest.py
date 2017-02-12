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

from tflearn.data_utils import load_csv
data, labels = load_csv('../data/training/training.csv', categorical_labels=True, n_classes=2)
data = preprocess(data, ignore_columns)


number_of_words = len(data[0])

# Build neural network
net = tflearn.input_data(shape=[None, number_of_words])
net = tflearn.fully_connected(net, 32)
net = tflearn.fully_connected(net, 32)
net = tflearn.fully_connected(net, 32)
net = tflearn.fully_connected(net, 32)
net = tflearn.fully_connected(net, 32)
net = tflearn.fully_connected(net, 2, activation='softmax')
net = tflearn.regression(net)


# Define model
model = tflearn.DNN(net)
# Start training (apply gradient descent algorithm)
model.fit(data, labels, n_epoch=100, snapshot_epoch=False, show_metric=True)

testdata, testlabels = load_csv('../data/training/test.csv', categorical_labels=True, n_classes=2)
testdata = preprocess(testdata, ignore_columns)

pred = model.predict(testdata)
hits = 0
total_ones = 0
for index, line in enumerate(pred):
    prediction = np.argmax(line)
    reality = testlabels[index][1]
    if reality == 1:
        total_ones += 1
        if prediction == reality:
            hits += 1

results = [[len(data), (float(hits)/total_ones)]]

print (results)
with open("../data/models/results.csv", "a") as resultFile:
    np.savetxt(resultFile, results, fmt='%.2f', delimiter=',', newline='\n')

print ('Accuracy: %s' % (float(hits)/total_ones))

# model.save('../data/models/model-' + str(datetime.datetime.now()) + '.tflearn')
