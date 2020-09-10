var loginInfoApp=new Vue({
    el: '#loginInfo',
    data: {
        isNotLogin: true,
        isConnected: false,
        stompClient: null,
        formItem: {
            username: '',
            password: ''
        },
        ruleValidate: {
            username: [
                { required: true, message: '用户名不能为空', trigger: 'blur' }
            ],
            password: [
                { required: true, message: '密码不能为空', trigger: 'blur' },
                { type: 'string', min: 6, message: 'The password length cannot be less than 6 bits', trigger: 'blur' }
            ]
        }
    },
    methods: {
        handleSubmit(name) {
            this.$refs[name].validate((valid) => {
                if (valid) {
                    this.isNotLogin=false;
                    var url = "/loginIn?username="+this.formItem.username+"&password="+this.formItem.password;
                    _this= this;
                    axios.post(url).then(function(result) {
                        _this.$Message.success(result.data);
                    });
                } else {
                    this.$Message.error('Fail!');
                }
            })
        },
        change (status) {
            if (status){
                // websocket的连接地址，此值等于WebSocketMessageBrokerConfigurer中registry.addEndpoint("/websocket-simple").withSockJS()配置的地址
                var socket = new SockJS('/websocket-rabbitmq');
                _this= this;
                stompClient = Stomp.over(socket);
                stompClient.connect({}, function(frame) {
                    _this.$Message.info('Connected: ' + frame);
                    // 客户端订阅消息的目的地址：此值BroadcastCtl中被@SendTo("/topic/getResponse")注解的里配置的值
                    // 客户端订阅消息的目的地址：此值BroadcastCtl中被@SendToUser("/topic/getResponse")注解的里配置的值 这是请求的地址必须使用/user前缀
                    stompClient.subscribe('/user/exchange/mytest/getMqResponse', function(respnose){
                        // stompClient.subscribe('/user/topic/getResponse', function(respnose){
                        _this.showResponse(respnose.body);
                    });
                });
            }else{
                if (stompClient != null) {
                    stompClient.disconnect();
                }
                this.$Message.info("Disconnected");
            }
        },
        showResponse(message) {
            var response = document.getElementById("response");
            response.innerHTML+=message + "</br>" + response.innerHTML;
        }
    }
});