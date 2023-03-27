const roomId = document.querySelector("#roomId");
const userId = document.querySelector("#userId");
const joinBtn = document.getElementById("joinBtn");
// 메시지 기능은 후순위
const localVideo = document.querySelector("#localVideo");
const remoteVideo = document.querySelector("#remoteVideo");

// websocket
const socketProtocol = (location.protocol==="https:")?"wss":"ws";
const socketUrl = `${socketProtocol}://${location.host}`;
let webSocket;

// stun
const rtcConfiguration = {
    iceServers: [
        { urls: 'stun:stun.l.google.com:19302' },
        { urls: 'stun:stun1.l.google.com:19302' },
        { urls: 'stun:stun2.l.google.com:19302' }
    ]
};

//media
const constraints ={
    video: {
        width : 700,
        height : 700,
    },audio : true
}

//dataChannel
const dataChannelOptions = {
    ordered: true, // 순서대로 전송할지 여부
    maxPacketLifeTime: 3000, // 패킷 수명
};

// webrtc
let peerConnection
let localStream;
let remoteStream;
let dataChannel;

joinBtn.addEventListener("click", ()=>{
    webSocket = new WebSocket(socketUrl);

    webSocket.onopen = async () => {
        console.log("시그널링 서버와 연결");
        // join 이벤트 구현 필요
        webSocket.send(JSON.stringify({type:"join", roomId}))

        peerConnection = new RTCPeerConnection(rtcConfiguration);

        navigator.mediaDevices.getUserMedia(constraints)
            .then(stream => {
                // Local 비디오 출력
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

                //데이터 채널 생성
                dataChannel = peerConnection.createDataChannel(roomId.innerHTML, dataChannelOptions);

                dataChannel.onopen = () => {
                    console.log('Data channel opened!');
                    dataChannel.send("아오 싯팔!");
                };

                dataChannel.onmessage = (event) => {
                    console.log(`Received message: ${event}`);
                    // Blob 생성
                    const blob = new Blob([event.data], { type: "video/webm" });
                    const videoURL = URL.createObjectURL(blob);

                    // Remote 비디오 출력
                    remoteVideo.src = videoURL;
                }
                dataChannel.onclose = () => {
                    console.log('Data channel closed.');
                };

                dataChannel.onerror = (error) => {
                    console.error(`Data channel error: ${error}`);
                };
            })
            .catch((error) => {
                console.log(`navigator.getUserMedia error: ${error}`);
            });



        // offer 생성
        peerConnection.createOffer(offer => {
            //promise 문법 비구현
            peerConnection.setLocalDescription(offer);

        }).then( res =>{
            console.log("local 등록", res.sdp);
            // 생성한 정보를 로컬에 등록
            peerConnection.setLocalDescription(res);
            console.log("send offer: ", res);
            // 이를 소켓에 전달해 시그널링 서버로 전송
            webSocket.send(JSON.stringify({type: "offer", roomId:roomId.innerHTML, userId:userId.innerHTML, data: res}));
        })

        // ICE candidate 정보를 생성하고 보냅니다.
        if(peerConnection.remoteDescription!==null){
            peerConnection.onicecandidate = event => {
                console.log("setlocaldescription 됐을 시 icecandidate: ", event)
                if (event.candidate) {
                    webSocket.send(JSON.stringify({ type: "iceCandidate", roomId:roomId.innerHTML, userId:userId.innerHTML, data: event.candidate }));
                }
            };
        }
    }

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
                webSocket.send(JSON.stringify({type:"answer", roomId:roomId.innerHTML, userId:userId.innerHTML,data:res}));
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

    webSocket.onclose = () => {
        console.log("시그널링 서버와 종료");
        webSocket.send(JSON.stringify({type:"leave", roomId:roomId.innerHTML, userId:userId.innerHTML}))
    }
})