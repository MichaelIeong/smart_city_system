package edu.fudan.se.sctap_lowcode_tool.constant;

public class Json_Example {
    public static final String json = """
            {
              "trigger": {
                "event":[
                    {
                        "event_type": "IllegalParking",
                        "params":  {
                            "location": "string",
                             "license": "string"
                        }
                    }
                 ],
                "filter": [
                  {
                    "location": {
                      "operator": "not in",
                      "targetLocation": "IllegalParking.ignoreLocationList"
                    }
                  }
                ]
              },
              "response": {
                "branch": [
                  {
                    "current_condition": [
                      {
                        "left": "location.NetworkAudioNum",
                        "operator": ">",
                        "right": "0"
                      }
                    ],
                    "chain": [
                      {
                        "ignore": {
                          "event_type": "IllegalParking",
                          "location": "location"
                        }
                      },
                      {
                        "branch": [
                          {
                            "history_condition": [
                              {
                                "left": "event_count(license, IllegalParking, 1, hour)",
                                "operator": ">",
                                "right": "0"
                              }
                            ],
                            "chain": [
                              {
                                "action": {
                                  "action_name": "IssueWorkOrder",
                                  "action_location": ["location"],
                                  "action_param": {
                                    "event_type": "IllegalParking",
                                    "location": "location",
                                    "data": "Vehicle illegal parking information"
                                  }
                                }
                              },
                              {
                                "wait": {
                                  "action_condition": {
                                    "event_type": "IllegalParking",
                                    "location": "location"
                                  }
                                }
                              },
                              {
                                "resume": {
                                  "event_type": "IllegalParking",
                                  "location": "location"
                                }
                              }
                            ]
                          },
                          {
                            "history_condition": [
                              {
                                "left": "event_count(license, IllegalParking, 1, hour)",
                                "operator": "==",
                                "right": "0"
                              }
                            ],
                            "chain": [
                              {
                                "action": {
                                  "action_name": "Broadcast",
                                  "action_location": ["location"],
                                  "action_param": {
                                    "event_type": "IllegalParking",
                                    "location": "location"
                                  }
                                }
                              },
                              {
                                "wait": {
                                  "time_condition": {
                                    "duration": "3",
                                    "unit": "minute"
                                  }
                                }
                              },
                              {
                                "resume": {
                                  "event_type": "IllegalParking",
                                  "location": "location"
                                }
                              }
                            ]
                          }
                        ]
                      }
                    ]
                  },
                  {
                    "current_condition": [
                      {
                        "left": "location.NetworkAudioNum",
                        "operator": "==",
                        "right": "0"
                      }
                    ],
                    "chain": [
                      {
                        "ignore": {
                          "event_type": "IllegalParking",
                          "location": "location"
                        }
                      },
                      {
                        "action": {
                          "action_name": "IssueWorkOrder",
                          "action_location": ["location"],
                          "action_param": {
                            "event_type": "IllegalParking",
                            "location": "location",
                            "data": "Vehicle illegal parking information"
                          }
                        }
                      },
                      {
                        "wait": {
                          "action_condition": {
                            "event_type": "IllegalParking",
                            "location": "location"
                          }
                        }
                      },
                      {
                        "resume": {
                          "event_type": "IllegalParking",
                          "location": "location"
                        }
                      }
                    ]
                  }
                ]
              }
            }
            """;
}
