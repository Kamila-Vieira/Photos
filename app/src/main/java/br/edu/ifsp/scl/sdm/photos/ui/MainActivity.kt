package br.edu.ifsp.scl.sdm.photos.ui

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.Toast
import br.edu.ifsp.scl.sdm.photos.R
import br.edu.ifsp.scl.sdm.photos.adapter.PhotoAdapter
import br.edu.ifsp.scl.sdm.photos.databinding.ActivityMainBinding
import br.edu.ifsp.scl.sdm.photos.model.JsonPlaceholderAPI
import br.edu.ifsp.scl.sdm.photos.model.Photo
import com.android.volley.toolbox.ImageRequest

class MainActivity : AppCompatActivity() {
    private val amb: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val photoList: MutableList<Photo> = mutableListOf()
    private val photoAdapter: PhotoAdapter by lazy {
        PhotoAdapter(this, photoList)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(amb.root)

        setSupportActionBar(amb.mainTb.apply {
            title = getString(R.string.app_name)
        })

        amb.photoTitlesSp.apply {
            adapter = photoAdapter
            onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    amb.photoIv.setImageBitmap(null)
                    amb.photoThumbnailIv.setImageBitmap(null)
                    retrievePhotoImage(photoList[position], true)
                    retrievePhotoImage(photoList[position], false)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // NSA
                }

            }
        }

        retrievePhotos()
    }

    private fun retrievePhotos() = JsonPlaceholderAPI.PhotoListRequest(
        { photos ->
            photos.also { photoAdapter.addAll(it) }
        },
        {
            Toast.makeText(this, getString(R.string.request_problem), Toast.LENGTH_SHORT).show()
        }
    ).also {
        JsonPlaceholderAPI.getInstance(this).addToRequestQueue(it)
    }

    private fun retrievePhotoImage(photo: Photo, isMainImage: Boolean) = ImageRequest(
        getValidImageUrl(if(isMainImage) photo.url else photo.thumbnailUrl, photo.title),
        { response ->
            if(isMainImage){
                amb.photoIv.setImageBitmap(response)
            }else{
                amb.photoThumbnailIv.setImageBitmap(response)
            }
        },
        0,
        0,
        ImageView.ScaleType.CENTER,
        Bitmap.Config.ARGB_8888,
        {
            println(it?.message)
            Toast.makeText(this, getString(R.string.request_problem), Toast.LENGTH_SHORT).show()
        }
    ).also {
        JsonPlaceholderAPI.getInstance(this).addToRequestQueue(it)
    }

    /** A função [getValidImageUrl] foi criada no intuito de adaptar a requisição de imagens
     * para a API "via.placeholder.com" que nesse momento não está funcionando, e para que isso
     * não impeça a correção da atividade a visualização de imagens foi implementada utilizando a API "placehold.co"
     * até que a API "via.placeholder.com" seja substituída **/
    private fun getValidImageUrl(imageUrl: String, imageTitle: String): String {
        if(!imageUrl.contains("https://via.placeholder.com/")) return imageUrl

        val urlPaths = imageUrl.replace("https://via.placeholder.com/", "")
        val (size, color) = urlPaths.split("/")
        return "https://placehold.co/$size" + "x$size/$color/white/jpg?text=$imageTitle"
    }
}