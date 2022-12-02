package com.example.shoppinglist;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.ContentValues.TAG;
import static android.os.Build.VERSION.SDK_INT;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.os.StrictMode;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilePermission;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity {
    private ListView ShoppingListLv;
    private EditText ItemEdit;
    private Button AddButton;
    private ArrayAdapter<String> Adapter;
    private Realm ShoppingRealm;
    private DataModel shopping = new DataModel();
    private DataModel ifExists = new DataModel();
    private EditText dbEt;
    private Context mcontext;
    private String clicked;
    private ArrayList<String> lista= new ArrayList<>();
    private String []splitedClicked = new String[100];
    private String item;
    DataModel find = new DataModel();
    private ImageButton shareButton;
    private static final int EXTERNAL_STORAGE_PERMISSION_CONSTANT = 100;
    private static final int REQUEST_PERMISSION_SETTING = 101;
    private static final int FILE_SELECT_CODE = 0;
    private boolean sentToSettings = false;
    private String line;
    private ImageButton importBtn;
    private SharedPreferences permissionStatus;
    private File file = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ShoppingListLv = findViewById(R.id.shopping_listView);
        ItemEdit = findViewById(R.id.item_editText);
        AddButton = findViewById(R.id.add_button);
        shareButton = findViewById(R.id.shareButton);
        importBtn = findViewById(R.id.importBtn);
        StrictMode.VmPolicy.Builder firstbuilder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(firstbuilder.build());
        permissionStatus = getSharedPreferences("permissionStatus",MODE_PRIVATE);
        Adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1){
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
               View view = super.getView(position, convertView, parent);
               TextView text = (TextView) view.findViewById(android.R.id.text1);

               return view;
            }
        };
        ShoppingListLv.setAdapter(Adapter);
        ShoppingRealm = Realm.getDefaultInstance();
        dbEt = findViewById(R.id.db_Et);
        registerForContextMenu(ShoppingListLv);
        mcontext = this;
        loadDatas();

        boolean readable = isExternalStorageReadable();
        boolean writeable = isExternalStorageWritable();
        //Toast.makeText(getBaseContext(), "is readable: " + readable, Toast.LENGTH_SHORT).show();
        //Toast.makeText(getBaseContext(), "is writeable : " + writeable, Toast.LENGTH_SHORT).show();
        /*if(!checkPermission()){
            Toast.makeText(this,"permissions are not granted",Toast.LENGTH_LONG).show();
        }*/

        if (ActivityCompat.checkSelfPermission(MainActivity.this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, WRITE_EXTERNAL_STORAGE)) {
                //Show Information about why you need the permission
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Need Storage Permission");
                builder.setMessage("This app needs storage permission.");
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{WRITE_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_PERMISSION_CONSTANT);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
             /*if (permissionStatus.getBoolean(WRITE_EXTERNAL_STORAGE,false)) {
                //Previously Permission Request was cancelled with 'Dont Ask Again',
                // Redirect to Settings after showing Information about why you need the permission
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Need Storage Permission");
                Toast.makeText(mcontext,"at begining",Toast.LENGTH_LONG).show();
                builder.setMessage("This app needs storage permission.");
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        sentToSettings = true;
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                        Toast.makeText(getBaseContext(), "Go to Permissions to Grant Storage", Toast.LENGTH_LONG).show();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }*/} else {
                //just request the permission
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{WRITE_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_PERMISSION_CONSTANT);
            }

            SharedPreferences.Editor editor = permissionStatus.edit();
            editor.putBoolean(WRITE_EXTERNAL_STORAGE,true);
            editor.apply();


        } else {
            //You already have the permission, just go ahead.
            proceedAfterPermission();
        }
        AddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShoppingRealm.executeTransaction(r -> {
                    ifExists = ShoppingRealm.where(DataModel.class).equalTo("name", ItemEdit.getText().toString()).findFirst();
                    if (ifExists != null) {
                        new AlertDialog.Builder(mcontext)
                                .setTitle(R.string.itemAleradyExists1)
                                .setMessage(R.string.itemAleradyExists2)

                                // Specifying a listener allows you to take an action before dismissing the dialog.
                                // The dialog is automatically dismissed when a dialog button is clicked.
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {


                                    }
                                })

                                // A null listener allows the button to dismiss the dialog and take no further action.
                                .setIcon(getResources().getDrawable(R.drawable.ic_baseline_priority_high_24))
                                .show();
                    } else {
                        String db;
                        boolean isEmpty = false;
                        if(!TextUtils.isEmpty(ItemEdit.getText())) {
                            shopping.setName(ItemEdit.getText().toString());
                        }else{
                            shopping.setName("");
                            isEmpty = true;
                        }
                        if (!TextUtils.isEmpty(dbEt.getText())) {
                            shopping.setAmount(Integer.valueOf(dbEt.getText().toString()));
                            db = dbEt.getText().toString();
                        } else {
                            db = Integer.toString(1);
                            shopping.setAmount(1);
                        }
                        if(!isEmpty) {
                            item = db.toString() + "   " + ItemEdit.getText().toString();
                        }else{
                            item = db.toString() + "   " + "--";
                        }
                        Adapter.add(item);
                        Adapter.notifyDataSetChanged();
                        ItemEdit.setText("");
                        dbEt.setText("");
                        ShoppingRealm.insertOrUpdate(shopping);
                    }
                });
            }
        });

        importBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFileChooser();
            }
        });


        ShoppingListLv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
                clicked = Adapter.getItem(position);
                splitedClicked = clicked.split(" +");
                return false;
            }
        });

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    writeToFile(mcontext);
                    File file = new File(mcontext.getFilesDir().getAbsolutePath()+"/shoppingList.txt");
                    Log.d("fileread",file.getAbsolutePath());
                    String filen = file.getAbsolutePath();
                    Uri uri = FileProvider.getUriForFile(mcontext,mcontext.getApplicationContext().getPackageName()+".provider",file);
                    Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                    sharingIntent.setType("text/plain");
                    sharingIntent.putExtra(Intent.EXTRA_STREAM, uri);
                    sharingIntent.putExtra(Intent.EXTRA_SUBJECT,
                            "Sharing File...");
                    sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    sharingIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    sharingIntent.addFlags(Intent.FLAG_RECEIVER_NO_ABORT);
                    sharingIntent.putExtra(Intent.EXTRA_TEXT, "Sharing File...");

                    startActivity(Intent.createChooser(sharingIntent, "share file with"));
                    //deleteFile(filen);
                }catch(Throwable e){
                    Toast.makeText(mcontext,e.toString(),Toast.LENGTH_LONG).show();
                }
            }
        });


    }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            super.onCreateContextMenu(menu, v, menuInfo);
            menu.setHeaderTitle("Choose your option");
            getMenuInflater().inflate(R.menu.menu, menu);

        }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.option_1:
                LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                PopupWindow pw = new PopupWindow(inflater.inflate(R.layout.editpopup, null, false),1000,1000, true);
                ((TextView)pw.getContentView().findViewById(R.id.ItemNameTv)).setText(splitedClicked[1]);
                ((TextView)pw.getContentView().findViewById(R.id.editPieces)).setText(splitedClicked[0]);
                ((Button)pw.getContentView().findViewById(R.id.EditBtn)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                       String darab= ((EditText)pw.getContentView().findViewById(R.id.editPieces)).getText().toString();
                       DataModel find = ShoppingRealm.where(DataModel.class).equalTo("name",splitedClicked[1]).findFirst();
                       ShoppingRealm.executeTransaction(r->{
                           find.setAmount(Integer.valueOf(darab));
                           ShoppingRealm.insertOrUpdate(find);
                       });
                    finish(); startActivity(getIntent());}
                });

                ((ImageButton)pw.getContentView().findViewById(R.id.closeBtn)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                            pw.dismiss();
                    }
                });

                pw.showAtLocation(this.findViewById(R.id.linlay), Gravity.CENTER, 0, 0);
                return true;
            case R.id.option_2:
                StringBuilder option= new StringBuilder();
                if(!splitedClicked[1].equals("--")) {
                    if(splitedClicked.length>=3) {
                        for (int i = 1; i < splitedClicked.length; i++) {
                            if(i == 1) {
                                option.append(splitedClicked[i]);
                            }
                            else{
                                option.append(" ").append(splitedClicked[i]);
                            }
                        }
                        find = ShoppingRealm.where(DataModel.class).equalTo("name", option.toString()).findFirst();
                    }else{
                        find = ShoppingRealm.where(DataModel.class).equalTo("name", splitedClicked[1]).findFirst();
                    }
                }else{
                    find = ShoppingRealm.where(DataModel.class).equalTo("name", "").findFirst();
                }
                ShoppingRealm.executeTransaction(r-> {
                    find.deleteFromRealm();
                    finish();
                    startActivity(getIntent());
                });

                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }






        public void loadDatas(){
        RealmResults<DataModel> results = ShoppingRealm.where(DataModel.class).findAll();
        for(DataModel result: results){
            String localItem;
            localItem = Integer.toString(result.getAmount())+ "   ";
            localItem += result.getName();

            Adapter.add(localItem);
            Adapter.notifyDataSetChanged();
        }

    }
    private void writeToFile(Context context) {
        String toShare = "";
        try {
            RealmResults<DataModel> dataToShare = ShoppingRealm.where(DataModel.class).findAll();
            for (int i=0;i<dataToShare.size();i++) {
                toShare += Integer.toString(dataToShare.get(i).getAmount());
                if(i==dataToShare.size()-1) {
                    toShare += " " + dataToShare.get(i).getName();
                }else{
                    toShare += " " + dataToShare.get(i).getName() + "\n";
                }
            }
        }catch (Throwable e){
            Toast.makeText(this,e.toString(),Toast.LENGTH_LONG).show();
        }
        String outDir = getFilesDir().getAbsolutePath();
        File file = new File(outDir,"shoppingList.txt");
        String filen = file.getPath();
        FileOutputStream fs = null;
        try {
            fs = new FileOutputStream(filen);
            fs.write(toShare.getBytes(StandardCharsets.UTF_16));
            Log.d("file",file.getAbsolutePath());
            fs.close();
        } catch (IOException e) {
            Toast.makeText(mcontext,"Write file: "+e.toString(),Toast.LENGTH_LONG).show();
        }

    }
    private void proceedAfterPermission() {
        //We've got the permission, now we can proceed further
        //Toast.makeText(getBaseContext(), "We got the Storage Permission", Toast.LENGTH_LONG).show();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == EXTERNAL_STORAGE_PERMISSION_CONSTANT) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //The External Storage Write Permission is granted to you... Continue your left job...
                proceedAfterPermission();
            }
        }
        if(requestCode == 2296){
            if (grantResults.length > 0) {
                boolean READ_EXTERNAL_STORAGE = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean WRITE_EXTERNAL_STORAGE = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                if (READ_EXTERNAL_STORAGE && WRITE_EXTERNAL_STORAGE) {
                    // perform action when allow permission success
                } else {
                    Toast.makeText(this, "Allow permission for storage access!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (sentToSettings) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                //Got Permission
                proceedAfterPermission();
            }
        }
    }

    private boolean isExternalStorageWritable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    // Checks if a volume containing external storage is available to at least read.
    private boolean isExternalStorageReadable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) ||
                Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED_READ_ONLY);
    }

    private void showFileChooser() {
        try {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("text/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);

            startActivityForResult(
                    Intent.createChooser(intent, "Select a File to Upload"),
                    FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(this, "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    InputStreamReader is = null;
                    BufferedReader bufferedReader = null;
                    String path = null;
                    try{
                    Uri uri = data.getData();
                    Log.d("Valami", "File Uri: " + uri.toString());
                    // Get the path
                        //is = new InputStreamReader(getContentResolver().openInputStream(uri));
                        //InputSource in = new InputSource(new InputStreamReader(getContentResolver().openInputStream(uri),"utf-16"));
                        bufferedReader = new BufferedReader(new InputStreamReader(getContentResolver().openInputStream(uri),"utf-16"));
                       while((line  = bufferedReader.readLine())!=null){
                                System.out.println(line);
                                ShoppingRealm.executeTransaction(r->{
                                String [] splitedLine = line.split(" +");
                                DataModel tempData = new DataModel();
                                    DataModel isExists = new DataModel();
                                if(splitedLine.length>2){
                                    String text = "";
                                    for(int i=1;i<splitedLine.length;i++){
                                        if(i == 1)
                                        {
                                            text+=splitedLine[i];
                                        }else
                                        text+=" "+splitedLine[i];
                                    }
                                    isExists = ShoppingRealm.where(DataModel.class).equalTo("name", text).findFirst();
                                }else {
                                    isExists = ShoppingRealm.where(DataModel.class).equalTo("name", splitedLine[1]).findFirst();
                                }
                                if(isExists == null) {
                                    tempData.setAmount(Integer.parseInt(splitedLine[0]));
                                    if (!splitedLine[1].equals("")) {
                                        if(splitedLine.length>2){
                                            String text = "";
                                            for(int i=1;i<splitedLine.length;i++){
                                                if(i==1) {
                                                    text +=splitedLine[i];
                                                }else{
                                                    text += " " + splitedLine[i];
                                                }
                                            }tempData.setName(text);
                                    }else{
                                            tempData.setName(splitedLine[1]);
                                        }
                                    }
                                    else {
                                        tempData.setName("");
                                    }
                                    ShoppingRealm.insertOrUpdate(tempData);
                                }else{
                                    isExists.setAmount(isExists.getAmount()+Integer.parseInt(splitedLine[0]));
                                    ShoppingRealm.insertOrUpdate(isExists);
                                }
                                });
                                //line = bufferedReader.readLine();

                            }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }finally {
                        if(bufferedReader!=null) {
                            try {
                                bufferedReader.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    Log.d("valami", "File Path: " + path);
                    // Get the file instance
                    // File file = new File(path);
                    // Initiate the upload
                    finish(); startActivity(getIntent());

                }
                break;
            case REQUEST_PERMISSION_SETTING: {
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        //Got Permission
                        proceedAfterPermission();
                        break;}}

            case 2296: {
                if (SDK_INT >= Build.VERSION_CODES.R) {
                    if (Environment.isExternalStorageManager()) {
                        // perform action when allow permission success
                    } else {
                        requestPermission();
                        //Toast.makeText(this, "Allow permission for storage access!", Toast.LENGTH_SHORT).show();
                    }
                }
                       break; }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public static String getPath(Context context, Uri uri) throws URISyntaxException {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = { "_data" };
            Cursor cursor = null;

            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                // Eat it
            }
        }
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }
        private void requestPermission() {
            if (SDK_INT >= Build.VERSION_CODES.R) {
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    intent.addCategory("android.intent.category.DEFAULT");
                    intent.setData(Uri.parse(String.format("package:%s",getApplicationContext().getPackageName())));
                    startActivityForResult(intent, 2296);
                } catch (Exception e) {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    startActivityForResult(intent, 2296);
                }
            } else {
                //below android 11
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{WRITE_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_PERMISSION_CONSTANT);
            }
        }

    private boolean checkPermission() {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            int result = ContextCompat.checkSelfPermission(MainActivity.this, READ_EXTERNAL_STORAGE);
            int result1 = ContextCompat.checkSelfPermission(MainActivity.this, WRITE_EXTERNAL_STORAGE);
            return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
        }
    }


}