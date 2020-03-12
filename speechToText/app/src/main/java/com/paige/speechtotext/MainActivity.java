package com.paige.speechtotext;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    EditText editText;

    //Speech Recognition
    SpeechRecognizer mSpeechRecognizer;
    Intent mSpeechRecognizerIntent;

    //Receive AudioStream
    TextToSpeech mTTS;
    SeekBar seekPitch;
    SeekBar seekSpeed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermission();
        editText = findViewById(R.id.editText);
        initializeVoiceRecognition();
        initializeTTS();
    }

    private void initializeVoiceRecognition(){
        //Speech Recognition
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        mSpeechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {


            }

            @Override
            public void onError(int i) {

            }

            @Override
            public void onResults(Bundle bundle) {
                ArrayList<String> matches = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if(matches != null){
                    editText.setText(matches.get(0));

                    Log.d("edittext value", editText.getText().toString());

                    Call<Message> sendMessage = RetrofitClient.getInstance().getApi()
                            .sendMessage(editText.getText().toString());

                    sendMessage.enqueue(new Callback<Message>() {
                        @Override
                        public void onResponse(Call<Message> call, Response<Message> response) {

                            Message message = new Message(response.body().message);
                            speak(message.message);

                        }

                        @Override
                        public void onFailure(Call<Message> call, Throwable t) {

                            Log.e("Network Error", t.getMessage());

                        }
                    });
                }
            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });

        //listen when user tapped on the button.
        findViewById(R.id.button).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                switch (motionEvent.getAction()){

                    case MotionEvent.ACTION_UP:
//                        mSpeechRecognizer.stopListening();
                        editText.setHint("You will see the input here");
                        break;

                    case MotionEvent.ACTION_DOWN:
                        editText.setText("");
                        editText.setHint("Listening...");
                        mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
                        break;

                }

                return false;
            }
        });
    }

    private void initializeTTS(){
        mTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS) {
                    int result = mTTS.setLanguage(Locale.ENGLISH);

                    if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                        Log.e("TTS", "Langauge not supported");
                    } else {
                        //잘됬을 때
                    }

                } else {
                    Log.e("TTS", "Initialization failed");
                }
            }
        });

        seekPitch = findViewById(R.id.seekPitch);
        seekSpeed = findViewById(R.id.seekSpeed);
    }
    //음성으로 실제로 읽어줌
    private void speak(String message){

        float pitch = (float) seekPitch.getProgress() / 50;
        if(pitch < 0.1) pitch = 0.1f;
        float speed = (float) seekSpeed.getProgress() / 50;
        if(speed < 0.1) speed = 0.1f;

        mTTS.setPitch(pitch);
        mTTS.setSpeechRate(speed);
        mTTS.speak(message, TextToSpeech.QUEUE_FLUSH, null);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTTS != null){
            mTTS.stop();
            mTTS.shutdown();
        }
    }

    private void checkPermission(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){

            boolean audioPermissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;

            if(!audioPermissionGranted){
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }

        }
    }

}
