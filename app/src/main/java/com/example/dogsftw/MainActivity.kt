 package com.example.dogsftw

import android.app.DownloadManager.Request
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.example.dogsftw.databinding.ActivityMainBinding
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors


 class MainActivity : AppCompatActivity() {

     private lateinit var kittenText: String
     lateinit var kittenUrl: String


     private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar2)

        fetchCatFacts().start()
        fetchCatImage().start()

        binding.button2.setOnClickListener{
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, kittenText +  "Image url: " + kittenUrl)

                println(kittenText)
                type = "text/plain"
            }

            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }

       binding.button.setOnClickListener{
            fetchCatFacts().start()
           fetchCatImage().start()
        }

    }

     private fun setCatImage(url: String?){
         val executor = Executors.newSingleThreadExecutor()
         val handler = Handler(Looper.getMainLooper())
         var catImage: Bitmap? = null

         executor.execute{

             try{
                 val `in` = java.net.URL(url).openStream()
                 catImage = BitmapFactory.decodeStream(`in`)

                 handler.post{
                     binding.imageView.setImageBitmap(catImage)
                 }

             }catch(e:java.lang.Exception){
                 e.printStackTrace()
             }
         }
     }

     private fun fetchCatFacts(): Thread {
         return Thread{
             var url = URL("https://cat-fact.herokuapp.com/facts/random")
             val connection = url.openConnection() as HttpURLConnection

             if(connection.responseCode == 200)
             {
                 val inputSystem = connection.inputStream
                 val inputStreamReader = InputStreamReader(inputSystem, "UTF-8")

                 val catFacts = Gson().fromJson(inputStreamReader, CatModel::class.java)
                 updateUI(catFacts)
                 inputStreamReader.close()
                 inputSystem.close()
             }
             else {
             println("Error retrieving stupid cats")
             }
         }
     }

     private fun fetchCatImage(): Thread {
         return Thread{
             var url = URL("https://api.thecatapi.com/v1/images/search")
             val connection = url.openConnection() as HttpURLConnection

             if(connection.responseCode == 200)
             {
                 val inputSystem = connection.inputStream
                 val inputStreamReader = InputStreamReader(inputSystem, "UTF-8")

                 val catImage: List<ImageModel> = Gson().fromJson(inputStreamReader, Array<ImageModel>::class.java).toList()
                 setCatImage(catImage[0].url)
                 kittenUrl= catImage[0].url.toString()

                 inputStreamReader.close()
                 inputSystem.close()
             }
             else {
                 println("Error retrieving stupid cats")
             }
         }
     }


     private fun updateUI(cat: CatModel?) {
         runOnUiThread {
             kotlin.run {
                 if (cat != null) {
                     if(cat.status?.verified == true){
                    binding.factText.text = cat.text
                     kittenText = cat.text.toString()
                         }
                     else {
                         fetchCatFacts().start()
                     }

                 }
             }
         }

     }
 }