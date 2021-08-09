package org.techtown.wishmatching

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_edit_profile.*
import java.text.SimpleDateFormat
import java.util.*

class EditProfileActivity : AppCompatActivity() {
    var PICK_IMAGE_FROM_ALBUM=0
    var storage : FirebaseStorage? = null
    var photoUri: Uri? = null // 이미지 URI 담을 수 있음
    var firestore : FirebaseFirestore? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        firestore = FirebaseFirestore.getInstance()

        myprofile_imgChange.setOnClickListener{    // 이미지 등록 버튼
            var photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type ="image/*"
            startActivityForResult(photoPickerIntent,PICK_IMAGE_FROM_ALBUM)
        }

        myprofile_doublecheck.setOnClickListener { //닉네입 중복 확인 버튼
            if(myprofile_doublecheck.text.toString() == "다시입력"){
                myprofile_nickname.isEnabled = true
                myprofile_doublecheck.text = "중복확인"
                return@setOnClickListener
            }
            if(myprofile_nickname.text.toString() == null){
                Toast.makeText(this, "닉네임을 입력해주세요", Toast.LENGTH_SHORT).show()
            } else{
                firestore!!.collection("user")
                    .whereEqualTo("nickname", myprofile_nickname.text.toString()) //uid
                    .get()
                    .addOnSuccessListener { documents->
                        if(documents.isEmpty){            // 처음 로그인 하면 프로필 화면으로 이동
                            Toast.makeText(this,"사용가능합니다", Toast.LENGTH_SHORT).show()
                            myprofile_doublecheck.text = "다시입력"
                            myprofile_nickname.isEnabled = false
                        } else{                        // 그게 아니라면 메인액티비티로 이동
                            Toast.makeText(this, "아이디가 중복됩니다", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
        myfinish_btn.setOnClickListener {// 완료버튼

            var timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())//파일이름 입력해주는 코드 - 이름이 중복 설정되지않도록 파일명을 날짜로
            var imageFileName = "IMAGE_"+timestamp+"_.png"
            var storageRef =storage?.reference?.child("user")?.child(imageFileName)

            if(photoUri == null){
                    Toast.makeText(this,"이미지를 선택해주세요", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            else if(myprofile_nickname.isEnabled == true){
                    Toast.makeText(this, "닉네임 중복을 확인해주세요", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
            }else{
                storageRef?.putFile(photoUri!!)?.addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        firestore!!.collection("user")
                            .whereEqualTo("uid", Authentication.auth.currentUser!!.uid)
                            .get()
                            .addOnSuccessListener { documents->
                                for(document in documents){
                                    firestore!!.collection("user").document(document.id).update(mapOf(
                                        "nickname" to myprofile_nickname.text.toString(),
                                        "imageUrl" to uri.toString()
                                    ))
                                }
                            }
                    }
                }
                Toast.makeText(this, "수정하였습니다.", Toast.LENGTH_SHORT).show()
                finish()
                }
            }
        }
//    fun contentUpload(){ // 파이어베이스 로드
//
//        var timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())//파일이름 입력해주는 코드 - 이름이 중복 설정되지않도록 파일명을 날짜로
//        var imageFileName = "IMAGE_"+timestamp+"_.png"
//
//        var storageRef =storage?.reference?.child("user")?.child(imageFileName)
////
//        callback 방식
//        //파일 업로드 //데이터베이스를 입력해주는코드
//        storageRef?.putFile(photoUri!!)?.addOnSuccessListener {
//            storageRef.downloadUrl.addOnSuccessListener { uri->
//                var contentDTO = ContentDTO()
//                contentDTO.uid = Authentication.auth.currentUser!!.uid
//                contentDTO.imageUrl = uri.toString()
//                contentDTO.nickname = edt_profile_nickname.text.toString()
//                firestore?.collection("user")?.document()?.set(contentDTO)
//                setResult(Activity.RESULT_OK)
//            }
//        } //파일업로드 성공 시 이미지 주소를 받아옴 ,받아오자마자 데이터 모델을 만듦듦
//    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode ==PICK_IMAGE_FROM_ALBUM)
            if(resultCode == Activity.RESULT_OK){  //사진을 선택했을 때 이미지의 경로가 이쪽으로 넘어옴
                photoUri = data?.data    //경로담기
                myprofile_img.setImageURI(photoUri)   //선택한 이미지로 변경
            }else{  //취소버튼 눌렀을 때 작동하는 부분
                finish()  //취소했을 때는 액티비티 그냥 취소
            }
    }



}