# Code Accompanying my Blog Post About Doing Weather Predictions with ML
See [Using Machine Learning to Predict the Weather in Basel](https://medium.com/@m.langer798/using-machine-learning-to-predict-the-weather-in-basel-pt-1-data-baselines-bb5c4d58458a)

## About the data
The historical weather data found in `src/main/resources/history_export_daily.csv` and 
`src/main/resources/history_export_hourly.csv` comes from [metoblue](https://www.meteoblue.com/de/wetter/archive/export/basel_schweiz_2661604).
Unfortunately, they have changed some columns in the data, so that if you download the data anew, some parts
of the code will no longer work.
