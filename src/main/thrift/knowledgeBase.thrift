namespace java com.knowledgebase.thrift.java
#@namespace scala com.knowledgebase.thrift

struct UserId {
    1: required i64 value
}

struct StockResource {
    1: required double currentValue
    2: required string timestamp
}

struct InfoResource {
    1: required string info
}

union Resource {
    1: StockResource stockResource,
    2: InfoResource infoResource
}

enum InterestType {
    STOCK,
    INFO
}

struct Interest {
    1: required string name
    2: required InterestType interestType
    3: required list<Resource> resources
}


struct GetInterestsRequest {
    1: required UserId userId
}

struct AddInterestsRequest {
    1: required UserId userId
    2: required list<Interest> interests
}

struct GetInterestsResponse {
    1: required bool isSuccess
    2: optional string errorMessage
    3: optional list<Interest> interests
}

struct SimpleResponse {
    1: required bool isSuccess
    2: optional string errorMessage
}

service KnowledgeBaseService {
    GetInterestsResponse getInterests(1: GetInterestsRequest request)
    SimpleResponse addInterests(1: AddInterestsRequest request)
}