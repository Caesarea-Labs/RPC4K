/**
 * Makes all optional fields non-undefined, because when the server returns them they are guaranteed to be defined.
 */
export type Response<T> = Promise<Full<T>>
/**
 * Makes all optional fields required. Does not affect nullable fields.
 */
export type Full<T> = {
    [P in keyof T]-?: Exclude<T[P], undefined>;
};
