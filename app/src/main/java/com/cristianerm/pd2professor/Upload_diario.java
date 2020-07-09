package com.cristianerm.pd2professor;

public class Upload_diario {

    private String mDate;
    private String mMensagem;
    private String mImageUrl;

    public Upload_diario() {
        //empty constructor needed
    }
    public Upload_diario(String hora_e_data, String mensagem, String imageUrl) {
        if (mensagem.trim().equals("")) {
            mensagem = "No Name";
        }
        mMensagem = mensagem;
        mImageUrl = imageUrl;
        mDate = hora_e_data;
    }

    public String getDate() {
        return mDate;
    }
    public void setDate(String date) {
        mDate = date;
    }

    public String getMensagem() {
        return mMensagem;
    }
    public void setMensagem(String mensagem) {
        mMensagem = mensagem;
    }
    public String getImageUrl() {
        return mImageUrl;
    }
    public void setImageUrl(String imageUrl) {
        mImageUrl = imageUrl;
    }

}
