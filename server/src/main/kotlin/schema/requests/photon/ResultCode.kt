package schema.requests.photon

enum class ResultCode(val value : Int){
    Ok(0),
    InternalError(1),
    InvalidRoomParameters(2)
}