package com.example.dean_mobile;

public class GetCam {
    private String local_;
    private String public_;

    public GetCam(String local_, String public_) {
        this.local_ = local_;
        this.public_ = public_;
    }

    public GetCam() {
    }

    public String getLocal_() {
        return local_;
    }

    public void setLocal_(String local_) {
        this.local_ = local_;
    }

    public String getPublic_() {
        return public_;
    }

    public void setPublic_(String public_) {
        this.public_ = public_;
    }
}
