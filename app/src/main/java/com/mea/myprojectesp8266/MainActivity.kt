package com.mea.myprojectesp8266

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.mea.myprojectesp8266.databinding.ActivityMainBinding
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class MainActivity : AppCompatActivity() {
     private lateinit var request: Request
     private lateinit var binding: ActivityMainBinding
     private lateinit var pref: SharedPreferences //Создаем класс который сохраняет ай пи
     private val client = OkHttpClient() //Создаем клиента
     override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        pref = getSharedPreferences("MyPref", MODE_PRIVATE) //Инициализируем класс, даем имя и кто имеет доступ
        onClickSaveIp()
        getIp()


        binding.apply {
            bLed1.setOnClickListener(onClickListener())
            bLed2.setOnClickListener(onClickListener())
            bLed3.setOnClickListener(onClickListener())
        }



     }



    override fun onCreateOptionsMenu(menu: Menu?): Boolean {  //Добовляем кнопку на рабочую поверхность
        menuInflater.inflate(R.menu.menu_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.sync) post("temperature") //Отправляем слово в пост(в микроконтроллер) температура
        return true
    }


    private fun onClickListener(): View.OnClickListener{
        return View.OnClickListener {
            when(it.id){
                R.id.bLed1 -> { post("led1") }  //Передаем в функцию post стрингу - led1
                R.id.bLed2 -> { post("led2") }
                R.id.bLed3 -> { post("led3") }
            }
        }
    }

    private fun getIp() = with(binding){ //Вытягиваем сохраненный ip
        val ip = pref.getString("ip", "") //Записываем в константу ip вытягиваем ее при помощи perf.getString под ключом ip
        if(ip != null){   //Если ip не равен 0, то ...
            if(ip.isNotEmpty()) edIp.setText(ip) //Так же если там не пустота, то берем строчку edIp и помещаем в него через setTexp (ip ключ)
        }
    }



    private fun onClickSaveIp() = with(binding){
        bSave.setOnClickListener {   // Нажимаем кнопку "SaveIp"
            if(edIp.text.isNotEmpty())saveIp(edIp.text.toString()) //Если в edIp ввели текст то выполнить функицю saveIp передаем в нее стрингу
        }
    }



    private fun saveIp(ip: String) { //Создаем функцию и передаем в нее стрингу ip
        val editor = pref.edit() //Создаем класс, который умеет записывать
        editor.putString("ip", ip) //ПутСтринг (Записать стрингу) указываем ключ айпи и предаем в нее ай пи
        editor.apply() //Приминяем (записываем)
    }


    private fun post(post: String){   // Отправка данных с интернета (стринга)
        Thread{  //Открываем второстепенный поток
            request = Request.Builder().url("http://${binding.edIp.text}/$post").build()  //Формироване запроса, вытягиваем ip + в конце дописываем сам запрос
            try {
                var response = client.newCall(request).execute()  //Создаем переменную которая будет отправлять данные в esp
                if(response.isSuccessful){  //Проверка на то, что все успешно
                    val resultText = response.body()?.string() // Принимаем данные и сохраняем в resultText берем от туда только текст
                    runOnUiThread {  //Обновляем вьюшки с второстепенного потока
                        val temp = resultText + "Cº"  // Создаем переменную и кладем в нее данные с resultText добовляем текст "C"
                        binding.tvTemp.text = temp   //Находим tvTemp и помещаем данные с temp
                    }
                }
            } catch (i: IOException){
            }
        }.start() //Старт второстепенного потока
    }


}