function makeplot() {
  Plotly.d3.text("results.csv", function(text) {
    var data = Plotly.d3.csv.parseRows(text).map(function(row) {
        return row.map(function(value) {
          return +value;
        });
    });
    processData(data);
  });
};

function processData(allRows) {
    allRows.sort(function compareRows(r1, r2){
      return r1[0] - r2[0];
    });

    console.log(allRows);
    var x = [], y = [], standard_deviation = [];

    for (var i=0; i<allRows.length; i++) {
        row = allRows[i];
        x.push( row[0] );
        y.push( row[1] * 0.8);
    }
    console.log( 'X',x, 'Y',y, 'SD',standard_deviation );
    makePlotly( x, y, standard_deviation );
}

function makePlotly( x, y, standard_deviation ){
    var plotDiv = document.getElementById("plot");
    var traces = [{
        x: x,
        y: y
    }];

    var layout = {
      title: 'Precision vs # days of training data',
      xaxis: {
        title: 'Number of days of training data',
        titlefont: {
          family: 'Courier New, monospace',
          size: 18,
          color: '#7f7f7f'
        }
      },
      yaxis: {
        title: 'Precision',
        titlefont: {
          family: 'Courier New, monospace',
          size: 18,
          color: '#7f7f7f'
        }
      }
    };

    Plotly.newPlot('myDiv', traces, layout);
};

makeplot();
