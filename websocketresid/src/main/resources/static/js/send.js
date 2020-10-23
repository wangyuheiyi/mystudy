var tableInfo=new Vue({
    el: '#sendInfo',
    data: {
        message:""
    },
    methods: {
        sendInfo: function (){
            var url = "/sendInfo?message="+this.message;
            _this= this;
            axios.get(url).then(function(result) {
                _this.$Message.success(result.data);
            });
        }
    }
});