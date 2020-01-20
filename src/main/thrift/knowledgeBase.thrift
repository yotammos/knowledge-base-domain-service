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

struct PollEntry {
    1: required string party
    2: required string candidate
    3: required double percentage
}

struct PollResource {
    1: required i32 cycle
    2: required string pollster
    3: required string fteGrade
    4: required i32 sampleSize
    5: required string officeType
    6: required string startDate
    7: required string endDate
    8: required string stage
    9: required list<PollEntry> entries
    10: optional string state
}

union Resource {
    1: StockResource stockResource,
    2: InfoResource infoResource,
    3: PollResource pollResource
}

enum InterestType {
    STOCK,
    INFO,
    POLL
}

struct Interest {
    1: required string name
    2: required InterestType interestType
    3: required list<Resource> resources
}

struct InterestInfo {
    1: required string name
    2: required InterestType interestType
}

struct SimpleRequest {
    1: required UserId userId
}

struct AddInterestsRequest {
    1: required UserId userId
    2: required list<Interest> interests
}

struct RemoveInterestsRequest {
    1: required UserId userId
    2: required list<string> interestNames
}

struct GetInterestsResponse {
    1: required bool isSuccess
    2: optional string errorMessage
    3: optional list<Interest> interests
}

struct GetInterestInfoResponse {
    1: required bool isSuccess
    2: optional string errorMessage
    3: optional list<InterestInfo> interestInfos
}

struct SimpleResponse {
    1: required bool isSuccess
    2: optional string errorMessage
}

service KnowledgeBaseService {
    GetInterestsResponse getInterests(1: SimpleRequest request)
    GetInterestInfoResponse getInterestInfo(1: SimpleRequest request)
    SimpleResponse addInterests(1: AddInterestsRequest request)
    SimpleResponse removeInterests(1: RemoveInterestsRequest request)
}