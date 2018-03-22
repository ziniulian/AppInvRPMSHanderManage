package com.invengo.rpms;

import android.app.Application;

public class MyApp extends Application{  
    private String userId;  
    private String userName; 
    private String deptCode; 
    private String deptName; 
    private String groupCode; 
    private String groupName; 
    private String postCode; 
    private String postName; 
    private String tel; 
  
    public String getUserId() {  
        return userId;  
    }  
    public void setUserId(String userId) {  
        this.userId = userId;  
    }  
    
    
    public String getUserName() {  
        return userName;  
    }  
    public void setUserName(String userName) {  
        this.userName = userName;  
    }  
    
    
    public String getDeptCode() {  
        return deptCode;  
    }  
    public void setDeptCode(String deptCode) {  
        this.deptCode = deptCode;  
    }  
    
  
    public String getDeptName() {  
        return deptName;  
    }  
    public void setDeptName(String deptName) {  
        this.deptName = deptName;  
    }  
     
    
    public String getGroupCode() {  
        return groupCode;  
    }  
    public void setGroupCode(String groupCode) {  
        this.groupCode = groupCode;  
    }  
      
    
    public String getGroupName() {  
        return groupName;  
    }   
    public void setGroupName(String groupName) {  
        this.groupName = groupName;  
    } 
    
        
    public String getPostCode() {  
        return postCode;  
    }    
    public void setPostCode(String postCode) {  
        this.postCode = postCode;  
    }  
    
    
    public String getPostName() {  
        return postName;  
    }  
    public void setPostName(String postName) {  
        this.postName = postName;  
    }  
    
    
    public String getTel() {  
        return tel;  
    }  
    public void setTel(String tel) {  
        this.tel = tel;  
    }   
}  
