// 로그에 계속 추가
const logBox = document.getElementById("logBox");
// 서버 URL
const serverUrl = "ws://172.30.1.7:8080";

// 서버 접속시 webSocket 객체 생성
const webSocket = new WebSocket(serverUrl);
console.log("웹소켓 생성: ",webSocket)


// offer SDP 정보를 생성하고 보냅니다.
//구글 스턴 서버 활용 예시
//스턴 서버를 통해 공인 ip를 알 수 있음
//turn 서버를 통해 방화벽이나 nat 문제로 통신할 수 없을 경우 대비 가능
const configuration = {
    iceServers: [
        {
            'urls': 'stun:stun.l.google.com:19302'
        },
        {
            'urls': 'turn:10.158.29.39:3478?transport=udp',
            'credential': 'XXXXXXXXXXXXX',
            'username': 'XXXXXXXXXXXXXXX'
        },
        {
            'urls': 'turn:10.158.29.39:3478?transport=tcp',
            'credential': 'XXXXXXXXXXXXX',
            'username': 'XXXXXXXXXXXXXXX'
        }
    ]
};

//RTC연결 객체 생성
const peerConnection = new RTCPeerConnection(configuration);
console.log("피어간 커넥션 객체 생성: ", peerConnection)
webSocket.onopen = ()=>{
    // offer 생성
    peerConnection.createOffer(offer => {
        //promise 문법 비구현
        peerConnection.setLocalDescription(offer)
    }).then( res =>{
        console.log("local 등록", res.sdp);
        // 생성한 정보를 로컬에 등록
        peerConnection.setLocalDescription(res);
        console.log("send offer: ", res);
        // 이를 소켓에 전달해 시그널링 서버로 전송
        webSocket.send(JSON.stringify({type: "offer", data: res}));
    })

    // ICE candidate 정보를 생성하고 보냅니다.
    peerConnection.onicecandidate = event => {
        console.log("setlocaldescription 됐을 시 icecandidate: ", event)
        if (event.candidate) {
            webSocket.send(JSON.stringify({ type: "iceCandidate", data: event.candidate }));
        }
    };
}

// 메시지 받았을 경우, 상대방에게 전달 되었을 경우
webSocket.onmessage = async event => {
    console.log("전달받은 메시지: ", event);

    const message = JSON.parse(event.data);
    if (message.type === "offer") {
        // answer SDP 정보를 받았으면, 이를 원격 경로로 설정
        console.log("offer 등록");
        await peerConnection.setRemoteDescription(new RTCSessionDescription(message.data))
        console.log("answer 생성")
            await peerConnection.createAnswer(answer=>{
                peerConnection.setLocalDescription(answer);
            }).then(res=>{
                console.log("answer 전달: ", res)
                webSocket.send(JSON.stringify({type:"answer", data:res}));
            })
    } else if (message.type === "iceCandidate") {
        // ice 타입이라면, ice에 추가
        console.log("icecandidate 등록")
        await peerConnection.addIceCandidate(new RTCIceCandidate(message.data));
    } else if(message.type==="answer"){
        console.log("answer 등록")
        await peerConnection.setRemoteDescription(new RTCSessionDescription(message.data));
    }
};

// ICE candidate 교환을 완료하면 Data Channel을 생성한다
const constraints ={
    video: {
        width : 700,
        height : 700,
    },audio : true
}

// Data Channel 생성
const dataChannel = peerConnection.createDataChannel("dataChannel");
console.log(dataChannel);
dataChannel.binaryType = "arraybuffer";

// Data Channel 열기
dataChannel.onopen = event => {
    console.log("데이터 채널 생성", event);
};

// 비디오 스트림 가져오기
navigator.mediaDevices.getUserMedia(constraints)
    .then(stream => {
        // Local 비디오 출력
        const localVideo = document.querySelector("#local_video");
        localVideo.srcObject = stream;

        // 비디오 트랙 가져오기
        const videoTrack = stream.getVideoTracks()[0];
        // 오디오 트랙 가져오기
        const audioTrack = stream.getAudioTracks()[0];

        // Video Track 전송
        const sender1 = peerConnection.addTrack(videoTrack, stream);
        console.log("Added video track to PeerConnection");
        // Audio Track 전송
        const sender2 = peerConnection.addTrack(audioTrack, stream);
        console.log("Added audio track to PeerConnection");

        // Data Channel에서 비디오 데이터 수신
        dataChannel.onmessage = event => {
            // Blob 생성
            const blob = new Blob([event.data], { type: "video/webm" });
            const videoURL = URL.createObjectURL(blob);

            // Remote 비디오 출력
            const remoteVideo = document.querySelector("#remote_video");
            remoteVideo.src = videoURL;
        };
    })
    .catch(error => {
        console.error(error);
    });