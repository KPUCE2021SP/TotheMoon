package org.techtown.wishmatching.Mypage.DealSituation

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.provider.PicassoProvider
import kotlinx.android.synthetic.main.activity_deal_situ.*
import kotlinx.android.synthetic.main.doingdeal_row.view.*
import org.techtown.wishmatching.Authentication
import org.techtown.wishmatching.Database.PostDTO
import org.techtown.wishmatching.R

class DealCompleteActivity : AppCompatActivity() {
    var firestore : FirebaseFirestore? = null
    var storage : FirebaseStorage? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deal_complete)
        supportActionBar?.title = "거래 완료 물품"
        storage = FirebaseStorage.getInstance() //스토리지 초기화
        firestore = FirebaseFirestore.getInstance()
        var data:MutableList<PostDTO> = mutableListOf()

        firestore
            ?.collection("post")!!
            .whereEqualTo("uid", Authentication.auth.currentUser!!.uid)
            .whereEqualTo("dealsituation", "dealComplete")
            .get()
            .addOnSuccessListener { documents->
                for(document in documents){
                    data.add(
                        PostDTO(document.get("documentId").toString(), document.get("imageUrl").toString(),
                        document.get("uid").toString(), document.get("title").toString(), document.get("content").toString(), document.get("category").toString())
                    )
                }
                var adapter = RecyclerViewAdapt(this)
                adapter.Postdata = data


                my_goods_Recyclerview.adapter = adapter
                my_goods_Recyclerview.layoutManager = LinearLayoutManager(this)
            }
    }
}
class RecyclerViewAdapt(val c: Context): RecyclerView.Adapter<RecyclerViewAdapt.ViewHolder>(){
    var Postdata = mutableListOf<PostDTO>()

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var mMenus: ImageView
        var firestore = FirebaseFirestore.getInstance()
        init{
            mMenus = itemView.findViewById(R.id.mMenus)
            mMenus.setOnClickListener { popupMenus(it) }
        }
        private fun popupMenus(v: View){  //팝업 메뉴
            val popupMenus = PopupMenu(c,v)
            popupMenus.inflate(R.menu.deal_complete_menu)
            popupMenus.setOnMenuItemClickListener {
                when(it.itemId){
                    R.id.return_doing_deal->{  //거래 중으로 변경을 누르면 해당 물품의 디비의 dealsituation의 dealComplete로 ->doingDeal로 변경,
                        firestore
                            ?.collection("post")!!
                            .whereEqualTo("documentId", itemView.documentID.text.toString())
                            .get()
                            .addOnSuccessListener { documents->
                                for (document in documents){
                                    firestore!!.collection("post").document(document.id).update(mapOf(
                                        "dealsituation" to "doingDeal"
                                    ))
                                }
                            }
                        itemView.card.visibility = View.GONE  //카드 사라지기.
                        true
                    }
                    else->true
                }
            }
            popupMenus.show()
            val popup = PopupMenu::class.java.getDeclaredField("mPopup")
            popup.isAccessible =true
            val menu = popup.get(popupMenus)
            menu.javaClass.getDeclaredMethod("setForceShowIcon",Boolean::class.java)
                .invoke(menu,true)
        }

        fun setPost(post : PostDTO){
            itemView.stuff_name.text = post.title.toString()
            PicassoProvider.get().load(post.imageUrl).into(itemView.doingdeal_row_image)
            itemView.documentID.text =post.documentId
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view = LayoutInflater.from(parent.context).inflate(R.layout.doingdeal_row,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = Postdata.get(position)
        holder.setPost(post)
    }
    override fun getItemCount(): Int {
        return Postdata.size
    }


}