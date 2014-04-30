package com.sungy.onegame.mclass;

import java.io.Serializable;

public class OneGameGame implements Serializable{
	private int id;
	private String game_name;
	private String publish_time;
	private String original_image;
	private String image;
	private String introduction;
	private String detail;
	private String detail_image;
	private String detail_original_image;
	private String download_url;
	private int comment_num;
	private int praise_num;
	private int is_delete;
	private int collect_num;
	private int share_num;
	
	public OneGameGame(){
		
	}

	public String getGame_name() {
		return game_name;
	}

	public void setGame_name(String game_name) {
		this.game_name = game_name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getPublish_time() {
		return publish_time;
	}

	public void setPublish_time(String publish_time) {
		this.publish_time = publish_time;
	}

	public String getOriginal_image() {
		return original_image;
	}

	public void setOriginal_image(String original_image) {
		this.original_image = original_image;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getIntroduction() {
		return introduction;
	}

	public void setIntroduction(String introduction) {
		this.introduction = introduction;
	}

	public String getDetail() {
		return detail;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}

	public String getDetail_image() {
		return detail_image;
	}

	public void setDetail_image(String detail_image) {
		this.detail_image = detail_image;
	}

	public String getDetail_original_image() {
		return detail_original_image;
	}

	public void setDetail_original_image(String detail_original_image) {
		this.detail_original_image = detail_original_image;
	}

	public int getComment_num() {
		return comment_num;
	}

	public void setComment_num(int comment_num) {
		this.comment_num = comment_num;
	}

	public int getPraise_num() {
		return praise_num;
	}

	public void setPraise_num(int praise_num) {
		this.praise_num = praise_num;
	}

	public String getDownload_url() {
		return download_url;
	}

	public void setDownload_url(String download_url) {
		this.download_url = download_url;
	}

	public int getIs_delete() {
		return is_delete;
	}

	public void setIs_delete(int is_delete) {
		this.is_delete = is_delete;
	}

	public int getCollect_num() {
		return collect_num;
	}

	public void setCollect_num(int collect_num) {
		this.collect_num = collect_num;
	}

	public int getShare_num() {
		return share_num;
	}

	public void setShare_num(int share_num) {
		this.share_num = share_num;
	}

}
