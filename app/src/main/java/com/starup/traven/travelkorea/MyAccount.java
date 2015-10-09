package com.starup.traven.travelkorea;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by moco-lab on 2015-09-14.
 */
public class MyAccount  extends AppCompatActivity {
    NumberPicker np;

    public int lang;
    public int age;
    public int gender;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_account);

        Intent data = getIntent();

//        lang = data.getIntExtra("lang", 0);
//        age = data.getIntExtra("age", 0);
//        gender = data.getIntExtra("gender", 0);

        readObject();

        if(lang == 1) {
            {
                TextView tv = (TextView) findViewById(R.id.id_nation);
                tv.setText("언어");
            }
            {
                TextView tv = (TextView) findViewById(R.id.id_gender);
                tv.setText("성별");
            }
            {
                TextView tv = (TextView) findViewById(R.id.id_age);
                tv.setText("나이");
            }
            {
                TextView tv = (TextView) findViewById(R.id.id_customize);
                tv.setText("추천 정보");
            }
        } else if(lang == 2) {
            {
                TextView tv = (TextView) findViewById(R.id.id_nation);
                tv.setText("语言");
            }
            {
                TextView tv = (TextView) findViewById(R.id.id_gender);
                tv.setText("性别");
            }
            {
                TextView tv = (TextView) findViewById(R.id.id_age);
                tv.setText("年龄");
            }
            {
                TextView tv = (TextView) findViewById(R.id.id_customize);
                tv.setText("推荐 信息");
            }
        } else if(lang == 3) {
            {
                TextView tv = (TextView) findViewById(R.id.id_nation);
                tv.setText("ランゲージ");
            }
            {
                TextView tv = (TextView) findViewById(R.id.id_gender);
                tv.setText("せいべつ");
            }
            {
                TextView tv = (TextView) findViewById(R.id.id_age);
                tv.setText("とし");
            }
            {
                TextView tv = (TextView) findViewById(R.id.id_customize);
                tv.setText("すいせん じょうほう");
            }
        } else {
            {
                TextView tv = (TextView) findViewById(R.id.id_nation);
                tv.setText("Language");
            }
            {
                TextView tv = (TextView) findViewById(R.id.id_gender);
                tv.setText("Sex");
            }
            {
                TextView tv = (TextView) findViewById(R.id.id_age);
                tv.setText("Age");
            }
            {
                TextView tv = (TextView) findViewById(R.id.id_customize);
                tv.setText("Recommend");
            }
        }



        // language spinner setting
        {
            Spinner spinner = (Spinner) findViewById(R.id.language_spinner);
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                    R.array.language_list, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);

            spinner.setSelection(lang);
            spinner.setOnItemSelectedListener(new LanguageOnItemSelectedListener());
        }
        // gender spinner setting
        {
            Spinner spinner = (Spinner) findViewById(R.id.gender_spinner);
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                    R.array.gender_list_en, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);

            spinner.setSelection(gender);

            spinner.setOnItemSelectedListener(new genderOnItemSelectedListener());
        }

        // age spinner
        {
            Spinner spinner = (Spinner) findViewById(R.id.age_spinner);

            List ageList = new ArrayList<Integer>();
            for (int i = 1; i <= 100; i++) {
                ageList.add(Integer.toString(i));
            }

            ArrayAdapter<Integer> spinnerArrayAdapter = new ArrayAdapter<Integer>(
                    this, android.R.layout.simple_spinner_item, ageList);
            spinnerArrayAdapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
            spinner.setAdapter(spinnerArrayAdapter);
            spinner.setSelection(age-1);
            spinner.setOnItemSelectedListener(new ageOnItemSelectedListener());
        }
//        // age picker setting
//        {
//            np = (NumberPicker) findViewById(R.id.age_picker);
//            np.setMinValue(1);
//            np.setMaxValue(150);
//            np.setWrapSelectorWheel(false);
//
//            np.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
//
//                @Override
//                public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
//                    age = newVal;
//                }
//            });
//        }
    }

    public void readObject() {
        try
        {
            String filePath = Environment.getExternalStorageDirectory().toString() + "/TravelKorea/info.ini";
            File file = new File(filePath);

            if(file.exists()) {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));

                lang = ois.readInt();
                age = ois.readInt();
                gender = ois.readInt();

                ois.close();
            }

        }catch (Exception e) {

        }
    }

    public void saveObject(){
        try
        {
            String folder_name = "TravelKorea";

            File f = new File(Environment.getExternalStorageDirectory(), folder_name);
            if (!f.exists()) {
                f.mkdirs();
            }

            //String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
            String filePath = Environment.getExternalStorageDirectory().toString() + "/TravelKorea/info.ini";

            File file = new File(filePath);
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file)); //Select where you wish to save the file...
            oos.writeInt(lang);
            oos.writeInt(age);
            oos.writeInt(gender);

            oos.flush(); // flush the stream to insure all of the information was written to 'save_object.bin'
            oos.close();// close the stream
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        finally {
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub

        saveObject();

        Intent intent = new Intent();
        intent.putExtra("lang", lang);
        intent.putExtra("age", age);
        intent.putExtra("gender", gender);

        setResult(RESULT_OK, intent);
        finish();

        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

    }


    public class LanguageOnItemSelectedListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {
            lang = pos;

            if(lang == 1) {
                Spinner spinner = (Spinner) findViewById(R.id.gender_spinner);
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(parent.getContext(),
                        R.array.gender_list_ko, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);

                {
                    TextView tv = (TextView) findViewById(R.id.id_nation);
                    tv.setText("언어");
                }
                {
                    TextView tv = (TextView) findViewById(R.id.id_gender);
                    tv.setText("성별");
                }
                {
                    TextView tv = (TextView) findViewById(R.id.id_age);
                    tv.setText("나이");
                }
                {
                    TextView tv = (TextView) findViewById(R.id.id_customize);
                    tv.setText("추천 정보");
                }
            } else if(lang == 2) {
                Spinner spinner = (Spinner) findViewById(R.id.gender_spinner);
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(parent.getContext(),
                        R.array.gender_list_ch, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);
                {
                    TextView tv = (TextView) findViewById(R.id.id_nation);
                    tv.setText("语言");
                }
                {
                    TextView tv = (TextView) findViewById(R.id.id_gender);
                    tv.setText("性别");
                }
                {
                    TextView tv = (TextView) findViewById(R.id.id_age);
                    tv.setText("年龄");
                }
                {
                    TextView tv = (TextView) findViewById(R.id.id_customize);
                    tv.setText("推荐 信息");
                }
            } else if(lang == 3) {
                Spinner spinner = (Spinner) findViewById(R.id.gender_spinner);
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(parent.getContext(),
                        R.array.gender_list_jp, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);
                {
                    TextView tv = (TextView) findViewById(R.id.id_nation);
                    tv.setText("ランゲージ");
                }
                {
                    TextView tv = (TextView) findViewById(R.id.id_gender);
                    tv.setText("せいべつ");
                }
                {
                    TextView tv = (TextView) findViewById(R.id.id_age);
                    tv.setText("とし");
                }
                {
                    TextView tv = (TextView) findViewById(R.id.id_customize);
                    tv.setText("すいせん じょうほう");
                }
            } else {
                Spinner spinner = (Spinner) findViewById(R.id.gender_spinner);
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(parent.getContext(),
                        R.array.gender_list_en, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);
                {
                    TextView tv = (TextView) findViewById(R.id.id_nation);
                    tv.setText("Language");
                }
                {
                    TextView tv = (TextView) findViewById(R.id.id_gender);
                    tv.setText("Sex");
                }
                {
                    TextView tv = (TextView) findViewById(R.id.id_age);
                    tv.setText("Age");
                }
                {
                    TextView tv = (TextView) findViewById(R.id.id_customize);
                    tv.setText("Recommend");
                }
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub
        }
    }

    public class ageOnItemSelectedListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {
            age = Integer.parseInt((String)parent.getItemAtPosition(pos));
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub
        }
    }

    public class genderOnItemSelectedListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {
            gender = pos;
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub
        }
    }

}
