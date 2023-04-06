console.log("This is my script file");

// function toggleSidebar(){
//     if($('.sidebar').is(":visible")){
//         //true
//         $(".sidebar").css("display","none");
//         $(".content").css("margin-left","0%");
//     }
//     else{
//         //false
//         $(".sidebar").css("display","block");
//         $(".content").css("margin-left","20%");
//     }
// };

function toggleSidebar() {
    
    const sidebar =  document.getElementsByClassName("sidebar")[0];
    const content =  document.getElementsByClassName("content")[0];

    if(window.getComputedStyle(sidebar).display === "none"){
        sidebar.style.display = "block";
        content.style.marginLeft = "20%";
    }
    else{
        sidebar.style.display = "none";
        content.style.marginLeft = "0%";
    }
}