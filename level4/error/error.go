package error

import (
  "encoding/json"
  "fmt"
  "net/http"
)

var errors map[int]string

const (
  EcodeKeyNotFound    = 100
  EcodeTestFailed     = 101
  EcodeNotFile        = 102
  EcodeNoMorePeer     = 103
  EcodeNotDir         = 104
  EcodeNodeExist      = 105
  EcodeKeyIsPreserved = 106
  EcodeRootROnly      = 107
  EcodeDirNotEmpty    = 108
  EcodeValueRequired        = 200
  EcodePrevValueRequired    = 201
  EcodeTTLNaN               = 202
  EcodeIndexNaN             = 203
  EcodeValueOrTTLRequired   = 204
  EcodeTimeoutNaN           = 205
  EcodeNameRequired         = 206
  EcodeIndexOrValueRequired = 207
  EcodeIndexValueMutex      = 208
  EcodeInvalidField         = 209

  EcodeRaftInternal = 300
  EcodeLeaderElect  = 301

  EcodeWatcherCleared    = 400
  EcodeEventIndexCleared = 401
)


func init() {
  errors = make(map[int]string)

  // command related errors
  errors[EcodeKeyNotFound] = "Key not found"
  errors[EcodeTestFailed] = "Compare failed" //test and set
  errors[EcodeNotFile] = "Not a file"
  errors[EcodeNoMorePeer] = "Reached the max number of peers in the cluster"
  errors[EcodeNotDir] = "Not a directory"
  errors[EcodeNodeExist] = "Key already exists" // create
  errors[EcodeRootROnly] = "Root is read only"
  errors[EcodeKeyIsPreserved] = "The prefix of given key is a keyword in sqlcluster"
  errors[EcodeDirNotEmpty] = "Directory not empty"

  // Post form related errors
  errors[EcodeValueRequired] = "Value is Required in POST form"
  errors[EcodePrevValueRequired] = "PrevValue is Required in POST form"
  errors[EcodeTTLNaN] = "The given TTL in POST form is not a number"
  errors[EcodeIndexNaN] = "The given index in POST form is not a number"
  errors[EcodeValueOrTTLRequired] = "Value or TTL is required in POST form"
  errors[EcodeTimeoutNaN] = "The given timeout in POST form is not a number"
  errors[EcodeNameRequired] = "Name is required in POST form"
  errors[EcodeIndexOrValueRequired] = "Index or value is required"
  errors[EcodeIndexValueMutex] = "Index and value cannot both be specified"
  errors[EcodeInvalidField] = "Invalid field"

  // raft related errors
  errors[EcodeRaftInternal] = "Raft Internal Error"
  errors[EcodeLeaderElect] = "During Leader Election"

  // sqlcluster related errors
  errors[EcodeWatcherCleared] = "watcher is cleared due to sqlcluster recovery"
  errors[EcodeEventIndexCleared] = "The event in requested index is outdated and cleared"

}

type Error struct {
  ErrorCode int    `json:"errorCode"`
  Message   string `json:"message"`
  Cause     string `json:"cause,omitempty"`
  Index     uint64 `json:"index"`
}

func NewError(errorCode int, cause string, index uint64) *Error {
  return &Error{
    ErrorCode: errorCode,
    Message:   errors[errorCode],
    Cause:     cause,
    Index:     index,
  }
}
func Message(code int) string {
  return errors[code]
}

// Only for error interface
func (e Error) Error() string {
  return e.Message
}

func (e Error) toJsonString() string {
  b, _ := json.Marshal(e)
  return string(b)
}
func (e Error) Write(w http.ResponseWriter) {
  w.Header().Add("X-Etcd-Index", fmt.Sprint(e.Index))
  // 3xx is raft internal error
  status := http.StatusBadRequest
  switch e.ErrorCode {
  case EcodeKeyNotFound:
    status = http.StatusNotFound
  case EcodeNotFile, EcodeDirNotEmpty:
    status = http.StatusForbidden
  case EcodeTestFailed, EcodeNodeExist:
    status = http.StatusPreconditionFailed
  default:
    if e.ErrorCode/100 == 3 {
      status = http.StatusInternalServerError
    }
  }
  http.Error(w, e.toJsonString(), status)
}
