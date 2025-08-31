package edu.fudan.se.sctap_lowcode_tool.constant;

public class JsonRuleExample {
    public static final String IllParkingRule = """
            {
                "trigger": {
                    "event_type": "ill_parking",
                    "event_params": {
                        "location": "string",
                        "plate_number": "string"
                    }
                },
                "response": {
                    "branch": [
                        {
                            "history_condition": {
                                "history_left": {
                                    "func": "event_count(ill_parking, 1, hour)",
                                    "func_params": {
                                        "plate_number": "plate_number"
                                    }
                                },
                                "operator": ">",
                                "right": "0"
                            },
                            "chain": [
                                {
                                    "action": {
                                        "action_name": "issue_work_order",
                                        "action_params": {
                                            "event_type": "ill_parking",
                                            "location": "location",
                                            "data": "Vehicle illegal parking information"
                                        }
                                    }
                                },
                                {
                                    "wait": {
                                        "action_wait": {
                                            "event_type": "ill_parking",
                                            "wait_params": {
                                                "location": "location"
                                            }
                                        }
                                    }
                                }
                            ]
                        },
                        {
                            "history_condition": {
                                "history_left": {
                                    "func": "event_count(ill_parking, 1, hour)",
                                    "func_params": {
                                        "plate_number": "plate_number"
                                    }
                                },
                                "operator": "==",
                                "right": "0"
                            },
                            "chain": [
                                {
                                    "branch": [
                                        {
                                            "current_condition": {
                                                "current_left": {
                                                    "type": "property",
                                                    "property": "NetworkAudioNum"
                                                },
                                                "operator": ">",
                                                "right": "0"
                                            },
                                            "chain": [
                                                {
                                                    "action": {
                                                        "action_name": "broadcast",
                                                        "action_params": {
                                                            "event_type": "ill_parking",
                                                            "location": "location",
                                                            "data": "You have parked illegally, please leave immediately"
                                                        }
                                                    }
                                                },
                                                {
                                                    "wait": {
                                                        "time_wait": {
                                                            "event_type": "ill_parking",
                                                            "duration": "3",
                                                            "unit": "minute",
                                                            "wait_params": {
                                                                "location": "location"
                                                            }
                                                        }
                                                    }
                                                }
                                            ]
                                        },
                                        {
                                            "current_condition": {
                                                "current_left": {
                                                    "type": "property",
                                                    "property": "NetworkAudioNum"
                                                },
                                                "operator": "==",
                                                "right": "0"
                                            },
                                            "chain": [
                                                {
                                                    "action": {
                                                        "action_name": "issue_work_order",
                                                        "action_params": {
                                                            "event_type": "ill_parking",
                                                            "location": "location",
                                                            "data": "Vehicle illegal parking information"
                                                        }
                                                    }
                                                },
                                                {
                                                    "wait": {
                                                        "action_wait": {
                                                            "event_type": "ill_parking",
                                                            "wait_params": {
                                                                "location": "location"
                                                            }
                                                        }
                                                    }
                                                }
                                            ]
                                        }
                                    ]
                                }
                            ]
                        }
                    ]
                }
            }
            """;
}
