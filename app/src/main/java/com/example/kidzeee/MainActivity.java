package com.example.kidzeee;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.crowdfire.cfalertdialog.CFAlertDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private ImageButton MainNextButton ;
    private List<KidzeeModel> KidzeeList;
    private static int CurrentItem=0;
    private TextToSpeech MainTextToSpeech;
    private RecyclerView SingleItemCorrectRecyclerView,SingleItemJumbledRecyclerView;
    private CorrectAdapter correctAdapter;
    private Toast WrongToast;
    private View WrongView;
    private SingleKidzeeAdapter singleKidzeeAdapter;
    private ImageView SingleItemImageView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        MainNextButton = findViewById(R.id.main_next_button);
        SingleItemImageView = findViewById(R.id.single_item_imageview);

        SingleItemJumbledRecyclerView = findViewById(R.id.single_item_jumbled_reyclerview);
        SingleItemJumbledRecyclerView.setHasFixedSize(true);
        SingleItemJumbledRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.HORIZONTAL,false));

        SingleItemCorrectRecyclerView = findViewById(R.id.single_item_correct_reyclerview);
        SingleItemCorrectRecyclerView.setHasFixedSize(true);
        SingleItemCorrectRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.HORIZONTAL,false));

        KidzeeList = new ArrayList<>();

        CurrentItem=0;
        animalList();
        LoadFirstItem();

        MainNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(CurrentItem < KidzeeList.size())
                {
                    try
                    {
                        findViewById(R.id.tick).setVisibility(View.INVISIBLE);
                        findViewById(R.id.tick_card).setVisibility(View.INVISIBLE);

                        CurrentItem++;
                        RequestOptions requestOptions=new RequestOptions().placeholder(R.drawable.ic_file_download_black_24dp);
                        Glide.with(getApplicationContext())

                                .load(KidzeeList.get(CurrentItem).getImageUri()).addListener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                return false;
                            }
                        }).apply(requestOptions)
                                .into(SingleItemImageView);

                        singleKidzeeAdapter = new SingleKidzeeAdapter(KidzeeList.get(CurrentItem).getOrgName(),GenerateRandomString(KidzeeList.get(CurrentItem).getOrgName()));
                        singleKidzeeAdapter.notifyDataSetChanged();
                        SingleItemJumbledRecyclerView.setAdapter(singleKidzeeAdapter);

                        correctAdapter= new CorrectAdapter(KidzeeList.get(CurrentItem).getOrgName(),0);
                        correctAdapter.notifyDataSetChanged();
                        SingleItemCorrectRecyclerView.setAdapter(correctAdapter);
                    }
                    catch (IndexOutOfBoundsException e)
                    {
                        Toast.makeText(getApplicationContext(),"Last item reached",Toast.LENGTH_SHORT).show();
                    }


                }

            }
        });

        MainTextToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS)
                {
                    int result = MainTextToSpeech.setLanguage(Locale.US);

                    if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)
                    {
                        Toast.makeText(getApplicationContext(),"Language not supported",Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"TextToSpeech initialization failed.",Toast.LENGTH_SHORT).show();

                }
            }
        });


        WrongToast = new Toast(this);
        WrongView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.wrong_view_layout,null);
        WrongToast.setView(WrongView);
        WrongToast.setGravity(Gravity.CENTER,0,0);

       findViewById(R.id.selectItem).setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
                 showDialogue();
           }
       });

         if(isConnectedToNet(getApplicationContext())){

         }else{
             CFAlertDialog.Builder builder = new CFAlertDialog.Builder(MainActivity.this)
                     .setDialogStyle(CFAlertDialog.CFAlertStyle.BOTTOM_SHEET)
                     .setTitle("No internet ")
                     .setMessage("No internet. Please turn it ON")
                     .setDialogBackgroundColor(MainActivity.this.getResources().getColor(R.color.cfdialog_button_white_text_color))
                     .setIcon(R.drawable.ic_cancel_black_24dp)
                     .addButton("  CANCEL ", -1, Color.RED,
                             CFAlertDialog.CFAlertActionStyle.POSITIVE, CFAlertDialog.CFAlertActionAlignment.CENTER, new DialogInterface.OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialog, int which) {
                             dialog.dismiss();
                         }
                     });

             builder.show();


         }

        findViewById(R.id.homeButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    finishAffinity();
                }
                System.exit(0);
            }
        });

    }

    private void LoadFirstItem() {


           findViewById(R.id.tick).setVisibility(View.INVISIBLE);
           findViewById(R.id.tick_card).setVisibility(View.INVISIBLE);
           Glide.with(getApplicationContext()).load(KidzeeList.get(CurrentItem).getImageUri()).into(SingleItemImageView);

           singleKidzeeAdapter = new SingleKidzeeAdapter(KidzeeList.get(CurrentItem).getOrgName(),GenerateRandomString(KidzeeList.get(CurrentItem).getOrgName()));
           singleKidzeeAdapter.notifyDataSetChanged();
           SingleItemJumbledRecyclerView.setAdapter(singleKidzeeAdapter);

           correctAdapter= new CorrectAdapter(KidzeeList.get(CurrentItem).getOrgName(),0);
           correctAdapter.notifyDataSetChanged();
           SingleItemCorrectRecyclerView.setAdapter(correctAdapter);
    }

    private void fruitList() {
        CurrentItem=0;
        KidzeeList.clear();
        KidzeeList.add(new KidzeeModel("android.resource://com.example.kidzeee/drawable/mango_","MANGO"));
        KidzeeList.add(new KidzeeModel("android.resource://com.example.kidzeee/drawable/apple_","APPLE"));
        KidzeeList.add(new KidzeeModel("android.resource://com.example.kidzeee/drawable/pineapple_","PINEAPPLE"));
        KidzeeList.add(new KidzeeModel("android.resource://com.example.kidzeee/drawable/orange_","ORANGE"));
        KidzeeList.add(new KidzeeModel("android.resource://com.example.kidzeee/drawable/watermelon_","WATERMELON"));
        KidzeeList.add(new KidzeeModel("android.resource://com.example.kidzeee/drawable/banana_","BANANA"));
        KidzeeList.add(new KidzeeModel("android.resource://com.example.kidzeee/drawable/peach_","PEACH"));
        KidzeeList.add(new KidzeeModel("android.resource://com.example.kidzeee/drawable/carrot_","CARROT"));
        KidzeeList.add(new KidzeeModel("android.resource://com.example.kidzeee/drawable/lemon_","LEMON"));
        LoadFirstItem();
    }


    private void animalList(){
        KidzeeList.clear();
        CurrentItem=0;
        KidzeeList.add(new KidzeeModel(Uri.parse("android.resource://com.example.kidzeee/drawable/lion_").toString(), "LION"));
        KidzeeList.add(new KidzeeModel("android.resource://com.example.kidzeee/drawable/tiger_","TIGER"));
        KidzeeList.add(new KidzeeModel("android.resource://com.example.kidzeee/drawable/goat_","GOAT"));
        KidzeeList.add(new KidzeeModel("android.resource://com.example.kidzeee/drawable/cow_","COW"));
        KidzeeList.add(new KidzeeModel("android.resource://com.example.kidzeee/drawable/cheetah_","CHEETAH"));
        KidzeeList.add(new KidzeeModel("android.resource://com.example.kidzeee/drawable/girraffe_","GIRAFFE"));
        KidzeeList.add(new KidzeeModel("android.resource://com.example.kidzeee/drawable/deer_","DEER"));
        KidzeeList.add(new KidzeeModel("android.resource://com.example.kidzeee/drawable/monkey_","MONKEY"));
        KidzeeList.add(new KidzeeModel("android.resource://com.example.kidzeee/drawable/buffallo_","BUFFALO"));
        LoadFirstItem();
    }

    public  boolean isConnectedToNet(Context c) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private String GenerateRandomString(String orgName) {

        int length = orgName.length();
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Random rnd = new Random();

        while(orgName.length()<10)
        {
            char c = chars.charAt(rnd.nextInt(chars.length()));

            if(!orgName.contains(String.valueOf(c)))
            {
                orgName=orgName+String.valueOf(c);
            }
        }


        return shuffle(orgName);
    }

    public String shuffle(String input){
        List<Character> characters = new ArrayList<Character>();
        for(char c:input.toCharArray()){
            characters.add(c);
        }
        StringBuilder output = new StringBuilder(input.length());
        while(characters.size()!=0){
            int randPicker = (int)(Math.random()*characters.size());
            output.append(characters.remove(randPicker));
        }

        return output.toString();
    }

    public class SingleKidzeeAdapter extends RecyclerView.Adapter<SingleKidzeeAdapter.SingleKidzeeViewholder>
    {
        String OrgString;
        String RandomString;
        int count;

        public SingleKidzeeAdapter(String orgString, String o) {

            OrgString = orgString;
            RandomString= o;
            count=0;
        }

        @NonNull
        @Override
        public SingleKidzeeViewholder onCreateViewHolder(@NonNull ViewGroup viewGroup, final int i) {
            return new SingleKidzeeAdapter.SingleKidzeeViewholder(LayoutInflater.from(getApplicationContext()).inflate(R.layout.single_item_layout_button,viewGroup,false));
        }

        @Override
        public void onBindViewHolder(@NonNull final SingleKidzeeViewholder singleKidzeeViewholder, final int i) {


            final char ch = RandomString.charAt(i);
            singleKidzeeViewholder.SingleItem.setText(String.valueOf(ch));
            SingleItemJumbledRecyclerView.setVisibility(View.VISIBLE);

            singleKidzeeViewholder.SingleItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(ch==(OrgString.charAt(count)))
                    {


                        if(WrongView.isShown())
                        {
                            WrongView.setVisibility(View.GONE);
                        }
                        count++;

                        singleKidzeeViewholder.SingleItem.setVisibility(View.GONE);
                        MainTextToSpeech.speak(String.valueOf(ch),TextToSpeech.QUEUE_ADD,null);
                        correctAdapter= new CorrectAdapter(OrgString,count);
                        correctAdapter.notifyDataSetChanged();
                        SingleItemCorrectRecyclerView.setAdapter(correctAdapter);


                    }
                    else
                    {
                        ShowCustomToast(0);
                        MainTextToSpeech.speak("Wrong",TextToSpeech.QUEUE_ADD,null);
                    }

                    if(count==OrgString.length())
                    {
                        MainTextToSpeech.speak(OrgString,TextToSpeech.QUEUE_ADD,null);
                        SingleItemJumbledRecyclerView.clearAnimation();
                        SingleItemJumbledRecyclerView.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(),android.R.anim.fade_out));
                        SingleItemJumbledRecyclerView.getAnimation().start();
                        SingleItemJumbledRecyclerView.setVisibility(View.INVISIBLE);
                        findViewById(R.id.tick).setVisibility(View.VISIBLE);
                        findViewById(R.id.tick_card).setVisibility(View.VISIBLE);


                    }


                }
            });
        }


        @Override
        public int getItemCount() {

            return RandomString.length();
        }

        public class SingleKidzeeViewholder extends RecyclerView.ViewHolder {

            Button SingleItem;

            public SingleKidzeeViewholder(@NonNull View itemView) {
                super(itemView);

                SingleItem = itemView.findViewById(R.id.single_item_button);
            }
        }
    }

    private void ShowCustomToast(int i) {

        WrongView.setVisibility(View.VISIBLE);
        WrongToast.setDuration(i);
        WrongToast.show();
    }


    @Override
    protected void onDestroy() {

        if(MainTextToSpeech!=null)
        {
            MainTextToSpeech.stop();
            MainTextToSpeech.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            finishAffinity();
        }
        System.exit(0);
    }

    private class CorrectAdapter extends RecyclerView.Adapter<CorrectAdapter.CorrectViewholder> {

        String CorrectString;
        int correctlength;

        public CorrectAdapter(String correctString, int correctlength) {
            CorrectString = correctString;
            this.correctlength = correctlength;
        }

        @NonNull
        @Override
        public CorrectViewholder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new CorrectAdapter.CorrectViewholder(LayoutInflater.from(getApplicationContext()).inflate(R.layout.single_item_layout_button,viewGroup,false));
        }

        @Override
        public void onBindViewHolder(@NonNull CorrectViewholder correctViewholder, int i) {

            if(correctlength>i)
            {
                correctViewholder.OrgButton.setText(String.valueOf(CorrectString.charAt(i)));
                correctViewholder.OrgButton.setBackgroundResource(R.drawable.circular_background_green);
            }
            else
            {
                correctViewholder.OrgButton.setText("_");
                correctViewholder.OrgButton.setBackgroundResource(R.drawable.circular_background_red);

            }

        }

        @Override
        public int getItemCount() {

            return CorrectString.length();
        }

        public class CorrectViewholder extends RecyclerView.ViewHolder {

            Button OrgButton;

            public CorrectViewholder(@NonNull View itemView) {
                super(itemView);

                OrgButton = itemView.findViewById(R.id.single_item_button);
            }
        }
    }

   public void showDialogue(){
       CFAlertDialog.Builder builder = new CFAlertDialog.Builder(MainActivity.this)
               .setDialogStyle(CFAlertDialog.CFAlertStyle.BOTTOM_SHEET)
               .setTitle("Choose your item")
               .setSingleChoiceItems(new String[]{"Animals",
                       "Fruits",}, 2, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialogInterface, int i) {
                       switch (i){
                           case 0:
                               animalList();
                               dialogInterface.dismiss();


                               break;

                           case 1:
                               fruitList();
                               dialogInterface.dismiss();

                               break;

                       }

                   }
               })
               .setDialogBackgroundColor(MainActivity.this.getResources().getColor(R.color.cfdialog_button_white_text_color))
               .setIcon(R.drawable.ic_check_circle_black_24dp)
               .addButton("  CANCEL ", -1, Color.BLUE, CFAlertDialog.CFAlertActionStyle.POSITIVE, CFAlertDialog.CFAlertActionAlignment.CENTER, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       dialog.dismiss();
                   }
               });

       builder.show();




   }
}
