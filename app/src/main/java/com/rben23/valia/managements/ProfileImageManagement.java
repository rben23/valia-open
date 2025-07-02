package com.rben23.valia.managements;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.rben23.valia.R;
import com.rben23.valia.ValiaSQLiteHelper;
import com.rben23.valia.models.Users;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;


public class ProfileImageManagement {
    private String uid;
    private Context context;
    private ValiaSQLiteHelper vsql;

    public ProfileImageManagement(Context context, String uid) {
        this.context = context;
        this.uid = uid;
        this.vsql = ValiaSQLiteHelper.getInstance(context);
    }

    public void getRemoteUserImageProfile(ImageView imageView) {
        // Mostramos la imagen predeterminada
        Bitmap defaultBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.img_default_user_image);
        imageView.setImageBitmap(defaultBitmap);

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");
        Query query = userRef.orderByChild("uid").equalTo(uid);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Users user = dataSnapshot.getValue(Users.class);
                        if (user != null) {
                            String base64Photo = user.getProfileImage();
                            Bitmap bitmap = convertBase64ToBitmap(base64Photo);
                            if (bitmap != null) {
                                Bitmap circularBitmap = getCircularBitmap(bitmap);
                                imageView.setImageBitmap(circularBitmap);
                                // Si el bitmap original es distinto al circular, se libera
                                if (bitmap != circularBitmap && !bitmap.isRecycled()) {
                                    bitmap.recycle();
                                }
                            }
                        }
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "ERROR: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void getLocalUserImageProfile(ImageView imageView) {
        Bitmap defaultBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.img_default_user_image);
        imageView.setImageBitmap(defaultBitmap);

        Users users = vsql.getUsersDAO().getCurrentUser();
        String base64Image = users.getProfileImage();

        if (base64Image != null && !base64Image.isEmpty()) {
            Bitmap bitmap = convertBase64ToBitmap(base64Image);

            if (bitmap != null) {
                Bitmap circularBitmap = getCircularBitmap(bitmap);
                imageView.setImageBitmap(circularBitmap);

                if (bitmap != circularBitmap && !bitmap.isRecycled()) {
                    bitmap.recycle();
                }
            }
        }
    }

    public Bitmap getLocalUserBitmapProfile() {
        Users users = vsql.getUsersDAO().getCurrentUser();
        String base64Image = users.getProfileImage();

        if (base64Image != null && !base64Image.isEmpty()) {
            Bitmap bitmap = convertBase64ToBitmap(base64Image);
            return bitmap != null ? getCircularBitmap(bitmap) : null;
        }

        return BitmapFactory.decodeResource(context.getResources(), R.drawable.img_default_user_image);
    }

    // Convertimos los bytes a Bitmap
    public Bitmap convertBase64ToBitmap(String base64) {
        if (base64 == null || base64.isEmpty()) {
            Log.e("ERROR:", "La cadena Base64 es null");
            return null;
        }

        try {
            // Descodificamos el string a un array de bytes
            byte[] decodeBytes = Base64.decode(base64, Base64.NO_WRAP);

            // Obtenemos las dimensiones
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(decodeBytes, 0, decodeBytes.length, options);

            // Decidimos la escala
            int reqWidth = 500;
            int inSampleSize = 1;
            if (options.outWidth > reqWidth) {
                inSampleSize = options.outWidth / reqWidth;
            }

            options.inJustDecodeBounds = false;
            options.inSampleSize = inSampleSize;
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodeBytes, 0, decodeBytes.length, options);

            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Bitmap getCircularBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            Log.e("ERROR:", "Bitmap es null y no es posible crear la imagen circular");
            return null;
        }

        // Dimensiones del circulo
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int size = Math.min(width, height);

        Bitmap circularBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(circularBitmap);

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        float radius = size / 2f;
        canvas.drawCircle(radius, radius, radius, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, (size - width) / 2f, (size - height) / 2f, paint);

        return circularBitmap;
    }

    public String convertBitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        byte[] byteArray = outputStream.toByteArray();

        return Base64.encodeToString(byteArray, Base64.NO_WRAP);
    }

    // Convertir a Bitmap
    public static Bitmap convertToBitmap(Context context, Uri imageUri) {
        Bitmap bitmap = null;
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }
}
