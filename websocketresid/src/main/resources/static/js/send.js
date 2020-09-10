var tableInfo=new Vue({
    el: '#sendInfo',
    data: {
        userName:""
    },
    methods: {
        sendInfo: function (){
            var url = "/sendInfo?userName="+this.userName;
            _this= this;
            axios.get(url).then(function(result) {
                _this.$Message.success(result.data);
            });
        }
    }
});