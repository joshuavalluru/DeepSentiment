#!/bin/bash
source ~/tensorflow/bin/activate
cd ~/git/DeepSentiment/python
python run_tensor-latest.py
cd ~/git/DeepSentiment/data/models/
open http://localhost:8888/plot.html
python -m SimpleHTTPServer 8888
