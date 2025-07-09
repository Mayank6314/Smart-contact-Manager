console.log("this is javscript");

const toggleSidebar = () => {
    if($(".sidebar").is(":visible")){
        //true
        //hide

        $(".sidebar").css("display", "none");
        $(".content").css("margin-left", "0%");
    }else{
        //false
        //show 
        $(".sidebar").css("display", "block");
        $(".content").css("margin-left", "20%");
    }
};